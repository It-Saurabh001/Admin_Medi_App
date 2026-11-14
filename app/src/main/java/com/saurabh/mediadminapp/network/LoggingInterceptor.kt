package com.saurabh.mediadminapp.network

import com.saurabh.mediadminapp.data.v1.network.RedactingLogger
import com.saurabh.mediadminapp.utils.AppLogger
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.util.concurrent.TimeUnit


/**
 * A custom Interceptor that logs network activity to AppLogger.
 */
class LoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.nanoTime()

        // Log Request
        val requestBody = request.body
        val bodyString = if (requestBody != null) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            buffer.readUtf8()
        } else null

        AppLogger.logRequest(
            method = request.method,
            url = request.url.toString(),
            headers = request.headers.toMultimap().mapValues { (k, v) -> if (k.equals("authorization", true)) "[REDACTED]" else v.joinToString(",") },
            body = if (request.url.toString().contains("password", true)) "[PROTECTED]" else bodyString?.let { RedactingLogger.redact(it) }
        )

        val response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            AppLogger.e(AppLogger.TAG_NETWORK, "HTTP FAILED: ${e.message}")
            throw e
        }

        val durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)

        // Log Response
        val responseBody = response.body
        val responseBodyString = responseBody.source().let {
            it.request(Long.MAX_VALUE)
            it.buffer.clone().readUtf8()
        }

        AppLogger.logResponse(
            code = response.code,
            url = response.request.url.toString(),
            durationMs = durationMs,
            body = RedactingLogger.redact(responseBodyString)
        )

        return response
    }
}
