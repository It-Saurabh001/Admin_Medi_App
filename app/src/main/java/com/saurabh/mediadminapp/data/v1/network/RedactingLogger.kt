package com.saurabh.mediadminapp.data.v1.network

import com.saurabh.mediadminapp.utils.AppLogger
import okhttp3.logging.HttpLoggingInterceptor

class RedactingLogger(
    private val sink: (String) -> Unit = { AppLogger.d(AppLogger.TAG_NETWORK, it) }
) : HttpLoggingInterceptor.Logger {
    override fun log(message: String) = sink(redact(message))

    companion object {
        private val JSON_SECRET = Regex(
            "\"(password|current_password|new_password|otp|access_token|refresh_token)\"\\s*:\\s*\"[^\"]*\""
        )
        private val AUTH_HEADER = Regex("(?im)^(authorization:\\s*).*$")
        private val BEARER = Regex("(?i)bearer\\s+[A-Za-z0-9._~+/=-]+")

        fun redact(message: String): String = message
            .replace(JSON_SECRET) { "\"${it.groupValues[1]}\":\"[REDACTED]\"" }
            .replace(AUTH_HEADER) { "${it.groupValues[1]}[REDACTED]" }
            .replace(BEARER, "Bearer [REDACTED]")
    }
}
