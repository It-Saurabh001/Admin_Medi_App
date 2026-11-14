package com.saurabh.mediadminapp.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    companion object {
        val EXCLUDED_PATHS = listOf(
            "/admin/login",
            "/admin/verifyOtp",
            "/admin/requestAdminPasswordReset",
            "/admin/resetAdminPasswordWithOtp"
        )

        private const val V1_MARKER = "api/v1/"

        /** True for requests to the v1 API (path contains /api/v1/). */
        fun isV1Path(encodedPath: String): Boolean = encodedPath.contains(V1_MARKER)

        /** Path relative to /api/v1/ without leading/trailing slashes. */
        fun v1RelativePath(encodedPath: String): String {
            val i = encodedPath.indexOf(V1_MARKER)
            val rel = if (i >= 0) encodedPath.substring(i + V1_MARKER.length) else encodedPath
            return rel.trim('/')
        }

        /** v1 endpoints that must not carry the access token. */
        fun isV1Excluded(method: String, encodedPath: String): Boolean {
            val rel = v1RelativePath(encodedPath)
            return rel.startsWith("auth/login") ||
                rel.startsWith("auth/verify-otp") ||
                rel.startsWith("auth/password-reset") ||
                rel.startsWith("auth/refresh") ||
                rel.startsWith("app-config") ||
                rel.startsWith("openapi.json") ||
                rel.startsWith("health")
        }
    }


    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // ── v1 API: bearer on everything except the public endpoints; no 422 special-casing ──
        if (isV1Path(path)) {
            if (originalRequest.header("Authorization") != null || isV1Excluded(originalRequest.method, path)) {
                return chain.proceed(originalRequest)
            }
            val v1Token = tokenManager.getAccessToken()
            if (v1Token.isNullOrBlank()) return chain.proceed(originalRequest)
            return chain.proceed(
                originalRequest.newBuilder().header("Authorization", "Bearer $v1Token").build()
            )
        }

        // ── legacy API (unchanged behavior) ──
        Log.d("TAG", "Intercepter--------------------------------------------------------------")

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
        if (response.code == 422) {
            Log.w("AuthInterceptor", "Received 422 for $path: corrupt or invalid JWT. Invalidating session.")
            tokenManager.invalidateSession()
        }
        return response
    }
}
