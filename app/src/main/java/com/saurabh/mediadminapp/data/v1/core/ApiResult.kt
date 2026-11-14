package com.saurabh.mediadminapp.data.v1.core

import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.data.v1.error.ErrorBody
import com.saurabh.mediadminapp.data.v1.error.ErrorCode
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T, val pagination: Pagination? = null) : ApiResult<T>()
    data class Failure(val error: AppError) : ApiResult<Nothing>()
}

/**
 * Wraps an enveloped call. For endpoints whose `data` is null (or 204/empty body), declare T as nullable
 * (e.g. `ApiEnvelope<Unit?>`): Success.data is then null.
 */
suspend fun <T> safeApiCall(block: suspend () -> Response<ApiEnvelope<T>>): ApiResult<T> =
    guarded {
        val response = block()
        if (response.isSuccessful) {
            @Suppress("UNCHECKED_CAST")
            ApiResult.Success(response.body()?.data as T)
        } else {
            ApiResult.Failure(parseApiError(response.code(), response.errorBody()?.string()))
        }
    }

suspend fun <T> safeApiCallPaged(block: suspend () -> Response<PagedEnvelope<T>>): ApiResult<List<T>> =
    guarded {
        val response = block()
        if (response.isSuccessful) {
            val body = response.body()
            ApiResult.Success(body?.data ?: emptyList(), body?.pagination)
        } else {
            ApiResult.Failure(parseApiError(response.code(), response.errorBody()?.string()))
        }
    }

/** Parses an error body (JSON, empty or non-JSON) into [AppError.Api]. */
fun parseApiError(httpStatus: Int, raw: String?): AppError.Api {
    val body: ErrorBody? = if (raw.isNullOrBlank()) null else try {
        V1Gson.instance.fromJson(raw, ErrorBody::class.java)
    } catch (_: Exception) {
        null
    }
    return AppError.Api(
        httpStatus = httpStatus,
        code = body?.code ?: ErrorCode.UNKNOWN,
        message = body?.message?.takeIf { it.isNotBlank() } ?: "HTTP $httpStatus",
        fields = body?.fields
    )
}

private suspend fun <R> guarded(block: suspend () -> ApiResult<R>): ApiResult<R> = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: SocketTimeoutException) {
    ApiResult.Failure(AppError.Timeout)
} catch (e: IOException) {
    ApiResult.Failure(AppError.Network)
} catch (e: Exception) {
    ApiResult.Failure(AppError.Unknown(e))
}
