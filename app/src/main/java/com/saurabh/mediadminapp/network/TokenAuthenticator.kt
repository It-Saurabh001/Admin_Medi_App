package com.saurabh.mediadminapp.network

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * TokenAuthenticator — handles 401 Unauthorized by silently rotating the
 * access token and retrying the original request.
 *
 * Three key correctness guarantees
 * ─────────────────────────────────
 * 1. No circular dependency / deadlock
 *    [authApiService] is backed by a dedicated, unauthenticated OkHttpClient
 *    that has NO authenticator and NO auth interceptor.  Any 401 returned by
 *    the refresh endpoint will therefore NOT re-enter this function — it will
 *    just propagate as a normal failure.
 *
 * 2. Thread safety — one refresh at a time
 *    A [ReentrantLock] serialises concurrent 401 responses so that exactly one
 *    thread calls the network.  All others acquire the lock *after* the first
 *    thread has already stored the new token, and they exit via the
 *    "another thread already rotated the token" fast-path — no extra network
 *    call needed.
 *
 * 3. No runBlocking(Dispatchers.IO) nesting
 *    OkHttp's Authenticator is already executed on an OkHttp dispatcher thread
 *    (not the main thread).  Wrapping with [runBlocking] gives us a coroutine
 *    scope to call the suspend refresh API.  Nesting Dispatchers.IO inside that
 *    runBlocking would dispatch to yet another thread unnecessarily and could
 *    starve the OkHttp thread pool under high load.
 */
class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val authApiService: ApiServices  // isolated client — no auth, no authenticator
) : Authenticator {

    // Serialises concurrent 401 responses: only one thread refreshes at a time.
    private val refreshLock = ReentrantLock()

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("TokenAuthenticator", "authenticate() triggered, response code: ${response.code}")

        // Guard 1: only handle 401 — other error codes are not our concern.
        if (response.code != 401) return null

        // Guard 2: abort after 2 consecutive 401s (original + one retry).
        // This prevents an infinite retry loop if the server rejects the newly
        // refreshed token for reasons unrelated to expiry (e.g. account banned).
        if (responseCount(response) >= 2) {
            Log.e("TokenAuthenticator", "Already retried once — aborting, clearing session")
            tokenManager.invalidateSession()
            return null
        }

        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            Log.e("TokenAuthenticator", "No refresh token available — clearing session")
            tokenManager.invalidateSession()
            return null
        }

        // ── Serialised token refresh ──────────────────────────────────────────
        // withLock() blocks until the lock is free, ensuring that if two threads
        // arrive here simultaneously only one fires the refresh network call.
        return refreshLock.withLock {

            // Double-check: after acquiring the lock, re-read the stored token.
            // If another thread already rotated it, the fresh token will differ
            // from the one that was on the failed request — use it immediately.
            val currentStoredToken = tokenManager.getAccessToken()
            val requestToken = response.request
                .header("Authorization")
                ?.removePrefix("Bearer ")
                ?.trim()

            if (currentStoredToken != null && currentStoredToken != requestToken) {
                // A concurrent thread already refreshed — reuse its result.
                Log.d("TokenAuthenticator", "Token already rotated by another thread, reusing")
                return@withLock response.request.newBuilder()
                    .header("Authorization", "Bearer $currentStoredToken")
                    .build()
            }

            // Network refresh call — runs inside runBlocking because
            // Authenticator.authenticate() is a blocking (non-suspend) function.
            // OkHttp guarantees this is called on a background thread, so
            // runBlocking is safe here; we intentionally do NOT specify
            // Dispatchers.IO to avoid unnecessary thread context switches.
            val newToken = runBlocking {
                refreshAccessToken(refreshToken)
            }

            if (newToken != null) {
                Log.d("TokenAuthenticator", "Token refreshed successfully — retrying request")
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            } else {
                // Refresh failed (server rejected, network error, etc.).
                // Wipe local credentials and signal the UI.
                Log.e("TokenAuthenticator", "Token refresh failed — invalidating session")
                tokenManager.invalidateSession()
                null  // returning null tells OkHttp to propagate the 401 as-is
            }
        }
    }

    /**
     * Calls the refresh endpoint on the isolated [authApiService] and returns
     * the new access token string, or null on any failure.
     *
     * Note: we pass the raw refresh token as a form field value — NOT prefixed
     * with "Bearer ".  The endpoint receives it as a normal field, not an
     * HTTP Authorization header.
     */
    private suspend fun refreshAccessToken(refreshToken: String): String? {
        return try {
            Log.d("TokenAuthenticator", "Calling /admin/refreshToken endpoint")
            val response = authApiService.refreshToken(refreshToken)

            if (response.isSuccessful && response.body() != null) {
                val newToken = response.body()!!.access_token
                if (newToken.isNullOrBlank()) {
                    Log.e("TokenAuthenticator", "Refresh response body had null/blank access_token")
                    return null
                }
                // Persist with commit() so the retried request on the OkHttp
                // thread reads the new value immediately (apply() is async and
                // would race against the retry).
                tokenManager.updateAccessToken(newToken)
                Log.d("TokenAuthenticator", "Access token rotated and persisted")
                newToken
            } else {
                Log.e("TokenAuthenticator", "Refresh endpoint returned HTTP ${response.code()}: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("TokenAuthenticator", "Exception calling refresh endpoint: ${e.message}", e)
            null
        }
    }

    /**
     * Counts the number of times a response has been retried by walking the
     * priorResponse chain.  A count ≥ 2 means we already retried once — abort.
     */
    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}