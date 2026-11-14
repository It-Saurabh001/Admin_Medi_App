package com.saurabh.mediadminapp.data.v1.repository

import com.saurabh.mediadminapp.data.v1.core.ApiResult
import com.saurabh.mediadminapp.data.v1.core.PageRequest
import com.saurabh.mediadminapp.data.v1.core.Pagination
import com.saurabh.mediadminapp.data.v1.dto.AuditLog
import com.saurabh.mediadminapp.data.v1.dto.DashboardSummary
import com.saurabh.mediadminapp.data.v1.dto.SalesReportRow
import com.saurabh.mediadminapp.data.v1.dto.SalesReportSummary
import com.saurabh.mediadminapp.data.v1.dto.Supplier
import com.saurabh.mediadminapp.data.v1.dto.SupplierCreateRequest
import com.saurabh.mediadminapp.data.v1.dto.SupplierUpdateRequest
import com.saurabh.mediadminapp.data.v1.dto.TopProduct

/** GET reports/sales result: rows + totals from the envelope `summary`. */
data class SalesReportResult(
    val rows: List<SalesReportRow>,
    val summary: SalesReportSummary?,
    val pagination: Pagination?
)

interface SupplierRepository {
    /** GET suppliers. Admin. */
    suspend fun list(q: String? = null, isActive: Boolean? = null, page: PageRequest = PageRequest()): ApiResult<List<Supplier>>

    /** GET suppliers/{supplier_id}. Admin. */
    suspend fun get(supplierId: String): ApiResult<Supplier>

    /** POST suppliers. Admin. */
    suspend fun create(request: SupplierCreateRequest): ApiResult<Supplier>

    /** PATCH suppliers/{supplier_id}. Admin. */
    suspend fun update(supplierId: String, request: SupplierUpdateRequest): ApiResult<Supplier>

    /** POST suppliers/{supplier_id}/deactivate (soft). Admin. */
    suspend fun deactivate(supplierId: String): ApiResult<Supplier>
}

interface ReportRepository {
    /** GET dashboard/summary. Admin. */
    suspend fun dashboardSummary(): ApiResult<DashboardSummary>

    /** GET reports/sales (groupBy = "day" | "product"; default last 30 days). Admin. */
    suspend fun salesReport(
        range: DateRange = DateRange(),
        groupBy: String? = null,
        page: PageRequest = PageRequest()
    ): ApiResult<SalesReportResult>

    /** GET reports/top-products. Admin. */
    suspend fun topProducts(range: DateRange = DateRange(), page: PageRequest = PageRequest()): ApiResult<List<TopProduct>>
}

interface AuditRepository {
    /** GET audit-logs. Admin. */
    suspend fun list(filter: AuditFilter = AuditFilter(), page: PageRequest = PageRequest()): ApiResult<List<AuditLog>>
}
