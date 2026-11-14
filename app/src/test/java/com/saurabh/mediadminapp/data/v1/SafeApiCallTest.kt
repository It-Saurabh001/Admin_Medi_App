package com.saurabh.mediadminapp.data.v1

import com.saurabh.mediadminapp.data.v1.core.ApiEnvelope
import com.saurabh.mediadminapp.data.v1.core.ApiResult
import com.saurabh.mediadminapp.data.v1.core.PagedEnvelope
import com.saurabh.mediadminapp.data.v1.core.Pagination
import com.saurabh.mediadminapp.data.v1.core.safeApiCall
import com.saurabh.mediadminapp.data.v1.core.safeApiCallPaged
import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.data.v1.error.ErrorCode
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import retrofit2.Response
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class SafeApiCallTest {
    private val json = "application/json".toMediaType()

    private fun err(code: Int, body: String): Response<ApiEnvelope<String>> =
        Response.error(code, body.toResponseBody(json))

    private fun failure(r: ApiResult<*>): AppError.Api {
        assertTrue(r is ApiResult.Failure)
        return (r as ApiResult.Failure).error as AppError.Api
    }

    @Test fun validationWithFields() = runBlocking {
        val r = safeApiCall<String> {
            err(400, """{"status":400,"message":"Validation failed","code":"VALIDATION_ERROR","fields":{"email":"is required"}}""")
        }
        val e = failure(r)
        assertEquals(400, e.httpStatus)
        assertEquals(ErrorCode.VALIDATION_ERROR, e.code)
        assertEquals("is required", e.fields?.get("email"))
    }

    @Test fun tokenExpired() = runBlocking {
        val e = failure(safeApiCall<String> { err(401, """{"status":401,"message":"Token has expired","code":"TOKEN_EXPIRED"}""") })
        assertEquals(ErrorCode.TOKEN_EXPIRED, e.code)
    }

    @Test fun outOfStock() = runBlocking {
        val e = failure(safeApiCall<String> {
            err(409, """{"status":409,"message":"Only 105 unit(s) available","code":"OUT_OF_STOCK","fields":{"quantity":"available: 105"}}""")
        })
        assertEquals(ErrorCode.OUT_OF_STOCK, e.code)
        assertEquals("available: 105", e.fields?.get("quantity"))
    }

    @Test fun unknownCodeMapsToUnknown() = runBlocking {
        val e = failure(safeApiCall<String> { err(418, """{"status":418,"message":"teapot","code":"SOMETHING_NEW"}""") })
        assertEquals(ErrorCode.UNKNOWN, e.code)
        assertEquals("teapot", e.message)
    }

    @Test fun emptyAndNonJsonErrorBodies() = runBlocking {
        val empty = failure(safeApiCall<String> { err(500, "") })
        assertEquals(ErrorCode.UNKNOWN, empty.code)
        assertEquals(500, empty.httpStatus)
        val html = failure(safeApiCall<String> { err(502, "<html>Bad gateway</html>") })
        assertEquals(ErrorCode.UNKNOWN, html.code)
    }

    @Test fun noContent204() = runBlocking {
        val raw = okhttp3.Response.Builder()
            .code(204).message("No Content").protocol(Protocol.HTTP_1_1)
            .request(Request.Builder().url("http://localhost/").build()).build()
        val r = safeApiCall<Unit?> { Response.success<ApiEnvelope<Unit?>>(null, raw) }
        assertTrue(r is ApiResult.Success)
        assertNull((r as ApiResult.Success).data)
    }

    @Test fun successUnwrapsData() = runBlocking {
        val r = safeApiCall<String> { Response.success(ApiEnvelope(200, "ok", "hello")) }
        assertEquals("hello", (r as ApiResult.Success).data)
    }

    @Test fun pagedKeepsPagination() = runBlocking {
        val r = safeApiCallPaged<String> {
            Response.success(PagedEnvelope(200, "ok", listOf("a", "b"), Pagination(20, 0, 57)))
        }
        r as ApiResult.Success
        assertEquals(listOf("a", "b"), r.data)
        assertEquals(57, r.pagination?.total)
    }

    @Test fun ioExceptionIsNetwork() = runBlocking {
        val r = safeApiCall<String> { throw IOException("boom") }
        assertEquals(AppError.Network, (r as ApiResult.Failure).error)
    }

    @Test fun timeoutIsTimeout() = runBlocking {
        val r = safeApiCall<String> { throw SocketTimeoutException("slow") }
        assertEquals(AppError.Timeout, (r as ApiResult.Failure).error)
    }
}
