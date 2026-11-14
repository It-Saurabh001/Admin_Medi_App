package com.saurabh.mediadminapp.data.v1.api

import com.saurabh.mediadminapp.data.v1.core.ApiEnvelope
import com.saurabh.mediadminapp.data.v1.core.PagedEnvelope
import com.saurabh.mediadminapp.data.v1.dto.AuditLog
import com.saurabh.mediadminapp.data.v1.dto.CancelOrderRequest
import com.saurabh.mediadminapp.data.v1.dto.DashboardSummary
import com.saurabh.mediadminapp.data.v1.dto.Invoice
import com.saurabh.mediadminapp.data.v1.dto.Order
import com.saurabh.mediadminapp.data.v1.dto.OrderEditRequest
import com.saurabh.mediadminapp.data.v1.dto.RejectOrderRequest
import com.saurabh.mediadminapp.data.v1.dto.ReturnOrderRequest
import com.saurabh.mediadminapp.data.v1.dto.Sale
import com.saurabh.mediadminapp.data.v1.dto.SalesReportRow
import com.saurabh.mediadminapp.data.v1.dto.Supplier
import com.saurabh.mediadminapp.data.v1.dto.SupplierCreateRequest
import com.saurabh.mediadminapp.data.v1.dto.SupplierUpdateRequest
import com.saurabh.mediadminapp.data.v1.dto.TopProduct
import com.saurabh.mediadminapp.data.v1.dto.VerifyPrescriptionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface OrdersApi {
    /** Users see only their own orders; `user_id` is admin-only. */
    @GET("orders")
    suspend fun listOrders(
        @Query("status") status: String? = null,
        @Query("product_id") productId: String? = null,
        @Query("user_id") userId: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("updated_since") updatedSince: String? = null,
        @QueryMap page: Map<String, String> = emptyMap()
    ): Response<PagedEnvelope<Order>>

    @GET("orders/{order_id}")
    suspend fun getOrder(@Path("order_id") orderId: String): Response<ApiEnvelope<Order>>

    @PATCH("orders/{order_id}")
    suspend fun editOrder(
        @Path("order_id") orderId: String,
        @Body body: OrderEditRequest
    ): Response<ApiEnvelope<Order>>

    @POST("orders/{order_id}/approve")
    suspend fun approveOrder(@Path("order_id") orderId: String): Response<ApiEnvelope<Order>>

    @POST("orders/{order_id}/reject")
    suspend fun rejectOrder(
        @Path("order_id") orderId: String,
        @Body body: RejectOrderRequest = RejectOrderRequest()
    ): Response<ApiEnvelope<Order>>

    @POST("orders/{order_id}/cancel")
    suspend fun cancelOrder(
        @Path("order_id") orderId: String,
        @Body body: CancelOrderRequest = CancelOrderRequest()
    ): Response<ApiEnvelope<Order>>

    @POST("orders/{order_id}/verify-prescription")
    suspend fun verifyPrescription(
        @Path("order_id") orderId: String,
        @Body body: VerifyPrescriptionRequest = VerifyPrescriptionRequest()
    ): Response<ApiEnvelope<Order>>

    @POST("orders/{order_id}/sell")
    suspend fun sellOrder(
        @Path("order_id") orderId: String,
        @Header("Idempotency-Key") idempotencyKey: String? = null
    ): Response<ApiEnvelope<Order>>

    @POST("orders/{order_id}/return")
    suspend fun returnOrder(
        @Path("order_id") orderId: String,
        @Body body: ReturnOrderRequest = ReturnOrderRequest()
    ): Response<ApiEnvelope<Order>>

    @GET("orders/{order_id}/invoice")
    suspend fun getInvoice(@Path("order_id") orderId: String): Response<ApiEnvelope<Invoice>>
}

interface SalesApi {
    @GET("sales")
    suspend fun listSales(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("product_id") productId: String? = null,
        @Query("user_id") userId: String? = null,
        @Query("order_id") orderId: String? = null,
        @Query("batch_id") batchId: String? = null,
        @Query("updated_since") updatedSince: String? = null,
        @QueryMap page: Map<String, String> = emptyMap()
    ): Response<PagedEnvelope<Sale>>
}

interface SuppliersApi {
    @GET("suppliers")
    suspend fun listSuppliers(
        @Query("q") q: String? = null,
        @Query("is_active") isActive: Boolean? = null,
        @QueryMap page: Map<String, String> = emptyMap()
    ): Response<PagedEnvelope<Supplier>>

    @POST("suppliers")
    suspend fun createSupplier(@Body body: SupplierCreateRequest): Response<ApiEnvelope<Supplier>>

    @GET("suppliers/{supplier_id}")
    suspend fun getSupplier(@Path("supplier_id") supplierId: String): Response<ApiEnvelope<Supplier>>

    @PATCH("suppliers/{supplier_id}")
    suspend fun updateSupplier(
        @Path("supplier_id") supplierId: String,
        @Body body: SupplierUpdateRequest
    ): Response<ApiEnvelope<Supplier>>

    @POST("suppliers/{supplier_id}/deactivate")
    suspend fun deactivateSupplier(@Path("supplier_id") supplierId: String): Response<ApiEnvelope<Supplier>>
}

interface ReportsApi {
    @GET("dashboard/summary")
    suspend fun dashboardSummary(): Response<ApiEnvelope<DashboardSummary>>

    /** group_by: day | product. Totals are in PagedEnvelope.summary (see SalesReportSummary). */
    @GET("reports/sales")
    suspend fun salesReport(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("group_by") groupBy: String? = null,
        @QueryMap page: Map<String, String> = emptyMap()
    ): Response<PagedEnvelope<SalesReportRow>>

    @GET("reports/top-products")
    suspend fun topProducts(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @QueryMap page: Map<String, String> = emptyMap()
    ): Response<PagedEnvelope<TopProduct>>
}

interface AuditApi {
    @GET("audit-logs")
    suspend fun listAuditLogs(
        @Query("entity") entity: String? = null,
        @Query("entity_id") entityId: String? = null,
        @Query("actor_id") actorId: String? = null,
        @Query("action") action: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @QueryMap page: Map<String, String> = emptyMap()
    ): Response<PagedEnvelope<AuditLog>>
}
