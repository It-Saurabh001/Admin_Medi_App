package com.saurabh.mediadminapp

import com.saurabh.mediadminapp.data.v1.core.Pagination
import com.saurabh.mediadminapp.data.v1.dto.SalesReportRow
import com.saurabh.mediadminapp.data.v1.dto.SalesReportSummary
import com.saurabh.mediadminapp.data.v1.dto.TopProduct
import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.data.v1.repository.DateRange
import java.time.LocalDate

/** Query for GET /reports/sales and /reports/top-products. Defaults mirror the server (last 30 days). */
data class ReportQuery(
    val from: LocalDate = LocalDate.now().minusDays(30),
    val to: LocalDate = LocalDate.now(),
    /** sales: `day` | `product` */
    val groupBy: String = "day",
    /** sales: `key` | `quantity` | `revenue`; top-products: `quantity` | `revenue` */
    val sort: String? = null,
    /** `asc` | `desc` */
    val order: String? = null,
) {
    val range: DateRange get() = DateRange(from, to)
}

data class SalesReportState(
    val query: ReportQuery = ReportQuery(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val rows: List<SalesReportRow> = emptyList(),
    val summary: SalesReportSummary? = null,
    val pagination: Pagination? = null,
    val endReached: Boolean = false,
    val loaded: Boolean = false,
    val error: String? = null,
    val appError: AppError? = null,
)

data class TopProductsState(
    val query: ReportQuery = ReportQuery(sort = "quantity", order = "desc"),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val rows: List<TopProduct> = emptyList(),
    val pagination: Pagination? = null,
    val endReached: Boolean = false,
    val loaded: Boolean = false,
    val error: String? = null,
    val appError: AppError? = null,
)
