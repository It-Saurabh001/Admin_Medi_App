package com.saurabh.mediadminapp.repository

import com.saurabh.mediadminapp.common.ResultState
import com.saurabh.mediadminapp.data.v1.core.ApiEnvelope
import com.saurabh.mediadminapp.data.v1.core.OrderStatus
import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.data.v1.error.ErrorCode
import com.saurabh.mediadminapp.data.v1.repository.DateRange
import com.saurabh.mediadminapp.data.v1.repository.OrderFilter
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.time.Instant
import java.time.LocalDate

class RepositorySupportTest {

    @Test fun queryOfDropsNullsAndFormats() {
        val m = queryOf(
            "a" to null, "b" to "", "c" to Instant.parse("2026-11-14T17:33:47Z"),
            "d" to LocalDate.of(2026, 1, 2), "e" to true, "f" to OrderStatus.SOLD
        )
        assertEquals(mapOf("c" to "2026-11-14T17:33:47Z", "d" to "2026-01-02", "e" to "true", "f" to "sold"), m)
    }

    @Test fun dateRangeToQuery() {
        assertEquals(mapOf("from" to "2026-01-01"), DateRange(from = LocalDate.of(2026, 1, 1)).toQuery())
        assertEquals(emptyMap<String, String>(), (null as DateRange?).toQuery())
    }

    @Test fun orderFilterToQuery() {
        val q = OrderFilter(status = OrderStatus.PENDING, userId = "UID_1", dateRange = DateRange(to = LocalDate.of(2026, 2, 3))).toQuery()
        assertEquals(mapOf("status" to "pending", "user_id" to "UID_1", "to" to "2026-02-03"), q)
    }

    @Test fun v1FlowMapsFailureToAppException() = runBlocking {
        val states = v1Flow<String> {
            Response.error<ApiEnvelope<String>>(409,
                """{"status":409,"message":"Only 1 left","code":"OUT_OF_STOCK","fields":{"quantity":"available: 1"}}"""
                    .toResponseBody("application/json".toMediaType()))
        }.toList()
        assertEquals(ResultState.Loading, states[0])
        val err = states[1] as ResultState.Error
        val app = (err.exception as AppException).error as AppError.Api
        assertEquals(ErrorCode.OUT_OF_STOCK, app.code)
        assertEquals("available: 1", app.fields?.get("quantity"))
    }

    @Test fun v1FlowSuccessRunsHook() = runBlocking {
        var seen: String? = null
        val states = v1Flow<String>(onSuccess = { seen = it }) { Response.success(ApiEnvelope(200, "ok", "x")) }.toList()
        assertTrue(states[1] is ResultState.Success)
        assertEquals("x", seen)
    }
}
