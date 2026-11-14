package com.saurabh.mediadminapp.network

import android.util.Log
import com.saurabh.mediadminapp.data.v1.core.AppEvent
import com.saurabh.mediadminapp.data.v1.core.AppEvents
import com.saurabh.mediadminapp.data.v1.error.ErrorCode
import com.saurabh.mediadminapp.data.v1.network.V1RefreshApi
import com.saurabh.mediadminapp.data.v1.network.peekCode
import dagger.Lazy
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.io.IOException
import kotlin.concurrent.withLock

/**
 * Handles 401s for both APIs. Legacy paths keep the old behavior; paths containing /api/v1/ use the
 * code-aware flow (TOKEN_EXPIRED -> refresh, anything else -> session invalid). Both flows share
 * [TokenManager.refreshLock], so they can never refresh concurrently.
 */
class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val apiServiceProvider: Lazy<ApiServices>,
    private val v1RefreshApi: () -> V1RefreshApi = { throw IllegalStateException("v1 refresh api not configured") },
    private val appEvents: AppEvents = AppEvents()
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) return null
        return if (AuthInterceptor.isV1Path(response.request.url.encodedPath)) {
            authenticateV1(response)
        } else {
            authenticateLegacy(response)
        }
    }

    // ── v1 ────────────────────────────────────────────────────────────────────

    private fun authenticateV1(response: Response): Request? {
        // Unauthenticated call (e.g. wrong login password): nothing to refresh, keep the session.
        val sentHeader = response.request.header("Authorization") ?: return null
        if (responseCount(response) >= 2) return failV1()
        if (peekCode(response) != ErrorCode.TOKEN_EXPIRED) return failV1()

        return tokenManager.refreshLock.withLock {
            val sentToken = sentHeader.removePrefix("Bearer ").trim()
            val stored = tokenManager.getAccessToken()
            if (!stored.isNullOrBlank() && stored != sentToken) {
                // Another thread (legacy or v1) already rotated the tokens.
                return@withLock response.request.newBuilder().header("Authorization", "Bearer $stored").build()
            }
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken.isNullOrBlank()) return@withLock failV1()
            try {
                val result = v1RefreshApi().refresh("Bearer $refreshToken").execute()
                val data = result.body()?.data
                when {
                    result.isSuccessful && data != null &&
                        data.accessToken.isNotBlank() && data.refreshToken.isNotBlank() -> {
                        tokenManager.saveSession(
                            accessToken = data.accessToken,
                            refreshToken = data.refreshToken,
                            role = data.role,
                            accountId = data.accountId,
                            accountType = data.accountType,
                            expiresInSeconds = data.expiresIn.toLong()
                        )
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${data.accessToken}")
                            .build()
                    }
                    result.code() == 401 || result.code() == 403 -> failV1()
                    else -> null // server error: keep session, request fails with the original 401
                }
            } catch (_: IOException) {
                null // offline: keep session
            }
        }
    }

    private fun failV1(): Request? {
        tokenManager.invalidateSession() // clears tokens + notifies legacy UI listeners
        appEvents.emit(AppEvent.SessionInvalid)
        return null
    }

    // ── legacy (unchanged behavior) ───────────────────────────────────────────

    private fun authenticateLegacy(response: Response): Request? {
        Log.d("TAG", "Authenticator =>--------------------------------------------------------------")
        Log.d("TAG", "authenticate() triggered, response code: ${response.code}")

        if (responseCount(response) >= 2) {
            Log.e("TAG", "Authenticator=>Already retried once — aborting, clearing session $response")
            tokenManager.invalidateSession()
            return null
        }
        val refreshToken = tokenManager.getRefreshToken()
        Log.d("TAG", "Authenticator=>Retrieved refresh token: ${if (refreshToken.isNullOrEmpty()) "null/empty" else "present"}")
        if (refreshToken.isNullOrEmpty()) {
            Log.e("TAG", "Authenticator=>No refresh token available — clearing session")
            tokenManager.invalidateSession()
            return null
        }
        return tokenManager.refreshLock.withLock {
            val currentStoredToken = tokenManager.getAccessToken()
            val requestToken = response.request
                .header("Authorization")
                ?.removePrefix("Bearer ")
                ?.trim()

            Log.d("TAG", "Authenticator=>Current stored token: ${if (currentStoredToken.isNullOrEmpty()) "null/empty" else "present"}")

            if (currentStoredToken != null && currentStoredToken != requestToken) {
                // A concurrent thread already refreshed — reuse its result.
                Log.d("TAG", "Authenticator=>Token already rotated by another thread, reusing")
                return@withLock response.request.newBuilder()
                    .header("Authorization", "Bearer $currentStoredToken")
                    .build()
            }
            val newToken = refreshAccessToken(refreshToken)

            if (newToken != null) {
                Log.d("TAG", "Authenticator=>Token refreshed successfully — retrying request")
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            } else {
                Log.e("TAG", "Authenticator=>Token refresh failed — invalidating session")
                tokenManager.invalidateSession()
                null
            }
        }
    }

    private fun refreshAccessToken(refreshToken: String): String? {
        return try {
            Log.d("TAG", "Calling /admin/refreshToken endpoint")

            val authApiService = apiServiceProvider.get()
            val response = authApiService.refreshToken("Bearer $refreshToken").execute()

            if (response.isSuccessful && response.body() != null) {
                val newToken = response.body()!!.access_token
                val newRefreshToken = response.body()!!.refresh_token
                val newRole = response.body()!!.role
                if (newToken.isNullOrBlank() || newRefreshToken.isNullOrBlank() ) {
                    Log.e("TAG", "Refresh response body had null/blank access_token")
                    return null
                }
                tokenManager.updateTokens(newToken, newRefreshToken, newRole)
                Log.d("TAG", "Authenticator=>Access token and refresh token rotated and persisted")
                newToken
            } else {
                Log.e("TAG", "Authenticator=>Refresh endpoint returned HTTP ${response.code()}: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("TAG", "Exception calling refresh endpoint: ${e.message}", e)
            null
        }
    }

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
