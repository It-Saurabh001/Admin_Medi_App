package com.saurabh.mediadminapp.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    companion object {
        val EXCLUDED_PATHS = listOf(
            "/admin/login",
            "/admin/create",
            "/admin/verifyOtp",
            "/admin/requestAdminPasswordReset",
            "/admin/resetAdminPasswordWithOtp"
        )
    }


    override fun intercept(chain: Interceptor.Chain): Response {


        Log.d("TAG", "Intercepter--------------------------------------------------------------")

        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath
        // Check if current URL is excluded
        val isExcluded = EXCLUDED_PATHS.any { path.startsWith(it) }
        // If excluded or no token, send request as is
        val token = tokenManager.getAccessToken()
        val hasValidToken = !token.isNullOrBlank()
        Log.d("TAG", "Intercepter =>Has valid token: $hasValidToken, Is excluded: $isExcluded")
        // If excluded or no valid token, proceed without auth

        if (isExcluded || !hasValidToken) {
            return chain.proceed(originalRequest)
        }

        // Add JWT token to Authorization header
        Log.d("TAG", "Intercepter=>Adding Bearer token for path: $path")
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer ${token}")
            .build()

        val response = chain.proceed(authenticatedRequest)
        Log.d("TAG", "Response code: ${response.code} for $path")
        if (response.code == 401) {
            Log.w("AuthInterceptor", "Intercepter=>Received 401 for $path, token might be invalid or expired")
        }
        return response
    }
}
