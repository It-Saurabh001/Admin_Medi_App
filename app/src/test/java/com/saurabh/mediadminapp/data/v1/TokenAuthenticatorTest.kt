package com.saurabh.mediadminapp.data.v1

import android.content.SharedPreferences
import com.saurabh.mediadminapp.data.v1.core.AppEvents
import com.saurabh.mediadminapp.data.v1.core.V1Gson
import com.saurabh.mediadminapp.data.v1.network.V1RefreshApi
import com.saurabh.mediadminapp.network.ApiServices
import com.saurabh.mediadminapp.network.AuthInterceptor
import com.saurabh.mediadminapp.network.TokenAuthenticator
import com.saurabh.mediadminapp.network.TokenManager
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class TokenAuthenticatorTest {

    /** Minimal in-memory SharedPreferences. */
    private class FakePrefs : SharedPreferences {
        private val map = LinkedHashMap<String, Any?>()
        override fun getAll(): MutableMap<String, *> = synchronized(map) { LinkedHashMap(map) }
        override fun getString(key: String, defValue: String?) = synchronized(map) { map[key] as? String ?: defValue }
        override fun getStringSet(key: String, defValues: MutableSet<String>?) = defValues
        override fun getInt(key: String, defValue: Int) = synchronized(map) { map[key] as? Int ?: defValue }
        override fun getLong(key: String, defValue: Long) = synchronized(map) { map[key] as? Long ?: defValue }
        override fun getFloat(key: String, defValue: Float) = synchronized(map) { map[key] as? Float ?: defValue }
        override fun getBoolean(key: String, defValue: Boolean) = synchronized(map) { map[key] as? Boolean ?: defValue }
        override fun contains(key: String) = synchronized(map) { map.containsKey(key) }
        override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) {}
        override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
            private val pending = LinkedHashMap<String, Any?>()
            private var clear = false
            override fun putString(k: String, v: String?) = apply { pending[k] = v }
            override fun putStringSet(k: String, v: MutableSet<String>?) = apply { pending[k] = v }
            override fun putInt(k: String, v: Int) = apply { pending[k] = v }
            override fun putLong(k: String, v: Long) = apply { pending[k] = v }
            override fun putFloat(k: String, v: Float) = apply { pending[k] = v }
            override fun putBoolean(k: String, v: Boolean) = apply { pending[k] = v }
            override fun remove(k: String) = apply { pending[k] = null }
            override fun clear() = apply { clear = true }
            override fun commit(): Boolean {
                synchronized(map) {
                    if (clear) map.clear()
                    pending.forEach { (k, v) -> if (v == null) map.remove(k) else map[k] = v }
                }
                return true
            }
            override fun apply() { commit() }
        }
    }

    private lateinit var server: MockWebServer
    private val v1Refreshes = AtomicInteger()
    private val legacyRefreshes = AtomicInteger()

    @Before fun setUp() {
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path.orEmpty()
                val auth = request.getHeader("Authorization")
                return when {
                    path.endsWith("/api/v1/auth/refresh") -> {
                        v1Refreshes.incrementAndGet()
                        Thread.sleep(250)
                        MockResponse().setResponseCode(200).setBody(
                            """{"status":200,"message":"Token refreshed","data":{"access_token":"new_access","refresh_token":"new_refresh","token_type":"Bearer","expires_in":3600,"role":"admin","account_type":"admin","account_id":"AID_1"}}"""
                        )
                    }
                    path.endsWith("/admin/refreshToken") -> {
                        legacyRefreshes.incrementAndGet()
                        MockResponse().setResponseCode(200).setBody(
                            """{"status":200,"message":"ok","access_token":"legacy_access","refresh_token":"legacy_refresh","role":"admin"}"""
                        )
                    }
                    auth == "Bearer new_access" || auth == "Bearer legacy_access" ->
                        MockResponse().setResponseCode(200).setBody("""{"status":200,"message":"ok","data":null}""")
                    else -> MockResponse().setResponseCode(401)
                        .setBody("""{"status":401,"message":"Token has expired","code":"TOKEN_EXPIRED"}""")
                }
            }
        }
        server.start()
    }

    @After fun tearDown() { server.shutdown() }

    private fun newTokenManager() = TokenManager(FakePrefs()).also {
        it.saveSession("old_access", "old_refresh", "admin", "AID_1", "admin", 3600)
    }

    private fun v1RefreshApi(): V1RefreshApi = Retrofit.Builder()
        .baseUrl(server.url("/api/v1/"))
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create(V1Gson.instance))
        .build().create(V1RefreshApi::class.java)

    private fun legacyApi(): ApiServices = Retrofit.Builder()
        .baseUrl(server.url("/"))
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(ApiServices::class.java)

    private fun client(tm: TokenManager) = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(tm))
        .authenticator(TokenAuthenticator(tm, dagger.Lazy { legacyApi() }, { v1RefreshApi() }, AppEvents()))
        .build()

    @Test fun fiveParallelV1401sTriggerExactlyOneRefresh() {
        val tm = newTokenManager()
        val client = client(tm)
        val pool = Executors.newFixedThreadPool(5)
        val start = CountDownLatch(1)
        val codes = java.util.Collections.synchronizedList(mutableListOf<Int>())
        repeat(5) {
            pool.execute {
                start.await()
                client.newCall(Request.Builder().url(server.url("/api/v1/products")).build())
                    .execute().use { codes.add(it.code) }
            }
        }
        start.countDown()
        pool.shutdown()
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS))

        assertEquals(listOf(200, 200, 200, 200, 200), codes.toList())
        assertEquals(1, v1Refreshes.get())
        assertEquals(0, legacyRefreshes.get())
        assertEquals("new_access", tm.getAccessToken())
        assertEquals("new_refresh", tm.getRefreshToken())
        assertEquals("admin", tm.getRole())
        assertEquals("AID_1", tm.getAccountId())
        assertEquals("admin", tm.getAccountType())
        assertTrue(tm.getExpiresAt() > System.currentTimeMillis())
    }

    @Test fun v1NonExpiredUnauthorizedClearsSession() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest) = MockResponse().setResponseCode(401)
                .setBody("""{"status":401,"message":"Invalid token","code":"TOKEN_INVALID"}""")
        }
        val tm = newTokenManager()
        client(tm).newCall(Request.Builder().url(server.url("/api/v1/products")).build()).execute().use {
            assertEquals(401, it.code)
        }
        assertNull(tm.getAccessToken())
        assertEquals(0, v1Refreshes.get())
    }

    @Test fun legacyPathStillUsesLegacyRefresh() {
        val tm = newTokenManager()
        client(tm).newCall(Request.Builder().url(server.url("/admin/getAllUsers")).build()).execute().use {
            assertEquals(200, it.code)
        }
        assertEquals(1, legacyRefreshes.get())
        assertEquals(0, v1Refreshes.get())
        assertEquals("legacy_access", tm.getAccessToken())
        assertEquals("legacy_refresh", tm.getRefreshToken())
    }

    @Test fun v1ExcludedPaths() {
        assertTrue(AuthInterceptor.isV1Excluded("POST", "/api/v1/auth/login"))
        assertTrue(AuthInterceptor.isV1Excluded("POST", "/api/v1/auth/password-reset/confirm"))
        assertTrue(AuthInterceptor.isV1Excluded("POST", "/api/v1/users"))
        assertFalse(AuthInterceptor.isV1Excluded("GET", "/api/v1/users"))
        assertFalse(AuthInterceptor.isV1Excluded("POST", "/api/v1/auth/logout"))
        assertTrue(AuthInterceptor.isV1Path("/api/v1/products"))
        assertFalse(AuthInterceptor.isV1Path("/admin/getAllUsers"))
    }
}
