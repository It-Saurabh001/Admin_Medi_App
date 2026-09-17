package com.saurabh.mediadminapp.network


import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route


class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val tempApiService: ApiServices
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("PERF_TRACE", "TokenAuthenticator.authenticate START [Thread: ${Thread.currentThread().name}]")
        if (response.code != 401) {
            Log.d("PERF_TRACE", "TokenAuthenticator.authenticate END (Not 401) [Thread: ${Thread.currentThread().name}]")
            return null
        }
        Log.d("TokenAuthenticator", "Received 401, attempting token refresh")
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            Log.e("TokenAuthenticator", "No refresh token available")
            tokenManager.clearTokens()
            Log.d("PERF_TRACE", "TokenAuthenticator.authenticate END (No refreshToken) [Thread: ${Thread.currentThread().name}]")
            return null
        }

        val result = synchronized(this) {
            val newAccessToken = tokenManager.getAccessToken()
            val requestAccessToken =
                response.request.header("Authorization")?.replace("Bearer ", "")

            if (newAccessToken != null && newAccessToken != requestAccessToken) {
                return@synchronized response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            }

            if (responseCount(response) >= 2) {
                tokenManager.clearTokens()
                Log.e("TokenAuthenticator", "Already retried, clearing tokens")
                return@synchronized null
            }

            val refreshedToken = runBlocking(Dispatchers.IO) {
                refreshAccessToken(refreshToken)
            }

            if (refreshedToken != null) {
                tokenManager.updateAccessToken(refreshedToken)
                Log.d("TokenAuthenticator", "Token refreshed, retrying request")
                response.request.newBuilder()
                    .header("Authorization", "Bearer $refreshedToken")
                    .build()
            } else {
                tokenManager.clearTokens()
                Log.e("TokenAuthenticator", "Token refresh failed")
                null
            }
        }
        Log.d("PERF_TRACE", "TokenAuthenticator.authenticate END [Thread: ${Thread.currentThread().name}]")
        return result
    }

    private suspend fun refreshAccessToken(refreshToken: String): String? {
        return try {
            Log.d("TokenAuthenticator", "Calling refresh token API")
            // No nested runBlocking here — this function is already called inside
            // the outer runBlocking in authenticate(), so we call the suspend API directly.
            val response = tempApiService.refreshToken("Bearer $refreshToken")

            if (response.isSuccessful && response.body() != null) {
                val newAccessToken = response.body()!!.access_token
                tokenManager.updateAccessToken(newAccessToken)
                Log.d("TokenAuthenticator", "Access token updated successfully")
                newAccessToken
            } else {
                Log.e("TokenAuthenticator", "Refresh token API failed: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("TokenAuthenticator", "Exception during token refresh: ${e.message}")
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }

}