package com.saurabh.mediadminapp.data.v1.network

import com.saurabh.mediadminapp.data.v1.core.AppEvent
import com.saurabh.mediadminapp.data.v1.core.AppEvents
import com.saurabh.mediadminapp.data.v1.core.V1Gson
import com.saurabh.mediadminapp.data.v1.error.ErrorBody
import com.saurabh.mediadminapp.data.v1.error.ErrorCode
import com.saurabh.mediadminapp.network.AuthInterceptor
import okhttp3.Interceptor
import okhttp3.Response

/** v1 paths only. Peeks the body (never consumes it). */
class ErrorEventInterceptor(private val events: AppEvents) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (!AuthInterceptor.isV1Path(request.url.encodedPath)) return response
        val status = response.code
        if (status != 401 && status != 403 && status != 429) return response
        val code = peekCode(response)
        when {
            status == 403 && code == ErrorCode.ACCOUNT_BLOCKED -> events.emit(AppEvent.AccountBlocked)
            status == 401 && code == ErrorCode.TOKEN_INVALID -> events.emit(AppEvent.SessionInvalid)
            status == 429 -> events.emit(AppEvent.RateLimited)
        }
        return response
    }
}

/** Reads the error `code` without consuming the response body. */
internal fun peekCode(response: Response): ErrorCode? = try {
    val raw = response.peekBody(256 * 1024).string()
    V1Gson.instance.fromJson(raw, ErrorBody::class.java)?.code
} catch (_: Exception) {
    null
}
