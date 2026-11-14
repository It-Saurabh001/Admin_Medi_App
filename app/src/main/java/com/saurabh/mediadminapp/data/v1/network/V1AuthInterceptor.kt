package com.saurabh.mediadminapp.data.v1.network

import okhttp3.Interceptor
import okhttp3.Response

class V1AuthInterceptor(private val store: V1TokenStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.header("Authorization") != null || isExcluded(request.method, request.url.encodedPath)) {
            return chain.proceed(request)
        }
        val token = store.getAccessToken()
        if (token.isNullOrBlank()) return chain.proceed(request)
        return chain.proceed(request.newBuilder().header("Authorization", "Bearer $token").build())
    }

    companion object {
        /** Path relative to /api/v1/ (prefix stripped if present). */
        fun relativePath(encodedPath: String): String {
            val marker = "api/v1/"
            val i = encodedPath.indexOf(marker)
            val rel = if (i >= 0) encodedPath.substring(i + marker.length) else encodedPath
            return rel.trim('/')
        }

        fun isExcluded(method: String, encodedPath: String): Boolean {
            val rel = relativePath(encodedPath)
            return rel.startsWith("auth/login") ||
                rel.startsWith("auth/verify-otp") ||
                rel.startsWith("auth/password-reset") ||
                rel.startsWith("auth/refresh") ||
                rel.startsWith("app-config") ||
                rel.startsWith("openapi.json") ||
                rel.startsWith("health") ||
                (method.equals("POST", ignoreCase = true) && rel == "users")
        }
    }
}
