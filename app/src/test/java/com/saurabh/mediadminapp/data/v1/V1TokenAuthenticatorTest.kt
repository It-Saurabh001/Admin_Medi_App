package com.saurabh.mediadminapp.data.v1

import com.saurabh.mediadminapp.data.v1.core.AppEvents
import com.saurabh.mediadminapp.data.v1.core.V1Gson
import com.saurabh.mediadminapp.data.v1.network.V1AuthInterceptor
import com.saurabh.mediadminapp.data.v1.network.V1RefreshApi
import com.saurabh.mediadminapp.data.v1.network.V1TokenAuthenticator
import com.saurabh.mediadminapp.data.v1.network.V1TokenStore
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class V1TokenAuthenticatorTest {

    private class FakeStore : V1TokenStore {
        @Volatile var access: String? = "old_access"
        @Volatile var refresh: String? = "old_refresh"
        @Volatile var role: String? = null
        @Volatile var accountId: String? = null
        val saves = AtomicInteger()
        override fun getAccessToken() = access
        override fun getRefreshToken() = refresh
        @Synchronized
        override fun saveSession(
            accessToken: String, refreshToken: String, role: String?,
            accountId: String?, accountType: String?, expiresInSeconds: Long?
        ) {
            access = accessToken; refresh = refreshToken; this.role = role; this.accountId = accountId
            saves.incrementAndGet()
        }
        override fun clear() { access = null; refresh = null }
    }

    private lateinit var server: MockWebServer
    private val refreshCalls = AtomicInteger()

    @Before fun setUp() {
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.path.orEmpty()
                return when {
                    path.endsWith("/auth/refresh") -> {
                        refreshCalls.incrementAndGet()
                        Thread.sleep(250)
                        MockResponse().setResponseCode(200).setBody(
                            """{"status":200,"message":"Token refreshed","data":{"access_token":"new_access","refresh_token":"new_refresh","token_type":"Bearer","expires_in":3600,"role":"admin","account_type":"admin","account_id":"AID_1"}}"""
                        )
                    }
                    request.getHeader("Authorization") == "Bearer new_access" ->
                        MockResponse().setResponseCode(200).setBody("""{"status":200,"message":"ok","data":null}""")
                    else -> MockResponse().setResponseCode(401)
                        .setBody("""{"status":401,"message":"Token has expired","code":"TOKEN_EXPIRED"}""")
                }
            }
        }
        server.start()
    }

    @After fun tearDown() { server.shutdown() }

    private fun refreshApi(): V1RefreshApi = Retrofit.Builder()
        .baseUrl(server.url("/api/v1/"))
        .client(OkHttpClient())
        .addConverterFactory(GsonConverterFactory.create(V1Gson.instance))
        .build().create(V1RefreshApi::class.java)

    @Test fun fiveParallel401sTriggerExactlyOneRefresh() {
        val store = FakeStore()
        val events = AppEvents()
        val client = OkHttpClient.Builder()
            .addInterceptor(V1AuthInterceptor(store))
            .authenticator(V1TokenAuthenticator(store, { refreshApi() }, events))
            .build()

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
        assertEquals(1, refreshCalls.get())
        assertEquals(1, store.saves.get())
        assertEquals("new_access", store.access)
        assertEquals("new_refresh", store.refresh)
        assertEquals("admin", store.role)
        assertEquals("AID_1", store.accountId)
    }

    @Test fun nonExpiredUnauthorizedClearsSession() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest) = MockResponse().setResponseCode(401)
                .setBody("""{"status":401,"message":"Invalid token","code":"TOKEN_INVALID"}""")
        }
        val store = FakeStore()
        val client = OkHttpClient.Builder()
            .addInterceptor(V1AuthInterceptor(store))
            .authenticator(V1TokenAuthenticator(store, { refreshApi() }, AppEvents()))
            .build()
        client.newCall(Request.Builder().url(server.url("/api/v1/products")).build()).execute().use {
            assertEquals(401, it.code)
        }
        assertEquals(null, store.access)
        assertEquals(0, refreshCalls.get())
    }

    @Test fun excludedPaths() {
        assertTrue(V1AuthInterceptor.isExcluded("POST", "/api/v1/auth/login"))
        assertTrue(V1AuthInterceptor.isExcluded("POST", "/api/v1/auth/password-reset/confirm"))
        assertTrue(V1AuthInterceptor.isExcluded("POST", "/api/v1/users"))
        assertFalse(V1AuthInterceptor.isExcluded("GET", "/api/v1/users"))
        assertFalse(V1AuthInterceptor.isExcluded("POST", "/api/v1/auth/logout"))
    }
}
