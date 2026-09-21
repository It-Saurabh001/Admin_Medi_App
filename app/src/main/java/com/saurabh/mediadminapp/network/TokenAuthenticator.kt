package com.saurabh.mediadminapp.network

import android.util.Log
import dagger.Lazy
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val apiServiceProvider: Lazy<ApiServices>
) : Authenticator {

    private val refreshLock = ReentrantLock()


    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("TAG", "Authenticator =>--------------------------------------------------------------")
        Log.d("TAG", "authenticate() triggered, response code: ${response.code}")

        if (response.code != 401) return null
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
        return refreshLock.withLock {
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