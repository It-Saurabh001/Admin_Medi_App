package com.saurabh.mediadminapp.data.v1

import com.saurabh.mediadminapp.data.v1.core.OrderStatus
import com.saurabh.mediadminapp.data.v1.core.PageRequest
import com.saurabh.mediadminapp.data.v1.core.Pagination
import com.saurabh.mediadminapp.data.v1.core.V1Gson
import com.saurabh.mediadminapp.data.v1.core.loadMore
import com.saurabh.mediadminapp.data.v1.error.ErrorCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class EnumsAndPagingTest {
    private data class Holder(val status: OrderStatus?, val at: Instant?, val code: ErrorCode?)

    @Test fun enumsTolerateUnknownValues() {
        val h = V1Gson.instance.fromJson(
            """{"status":"brand_new","at":"2026-11-14T17:33:47Z","code":"NOPE"}""", Holder::class.java
        )
        assertEquals(OrderStatus.UNKNOWN, h.status)
        assertEquals(ErrorCode.UNKNOWN, h.code)
        assertEquals(Instant.parse("2026-11-14T17:33:47Z"), h.at)
        val known = V1Gson.instance.fromJson("""{"status":"sold","code":"OUT_OF_STOCK"}""", Holder::class.java)
        assertEquals(OrderStatus.SOLD, known.status)
        assertEquals(ErrorCode.OUT_OF_STOCK, known.code)
        assertNull(known.at)
    }

    @Test fun badTimestampIsNull() {
        assertNull(V1Gson.instance.fromJson("""{"at":"not-a-date"}""", Holder::class.java).at)
    }

    @Test fun queryMap() {
        val m = PageRequest(limit = 500, offset = 40, sort = "name", order = "asc").toQueryMap()
        assertEquals(mapOf("limit" to "100", "offset" to "40", "sort" to "name", "order" to "asc"), m)
        assertEquals(mapOf("limit" to "20", "offset" to "0"), PageRequest().toQueryMap())
    }

    @Test fun loadMore() {
        val first = PageRequest()
        val next = first.loadMore(Pagination(20, 0, 57))
        assertEquals(20, next?.offset)
        assertEquals(40, next?.loadMore(Pagination(20, 20, 57))?.offset)
        assertNull(PageRequest(offset = 40).loadMore(Pagination(20, 40, 57)))
        assertNull(first.loadMore(Pagination(20, 0, 20)))
    }
}
