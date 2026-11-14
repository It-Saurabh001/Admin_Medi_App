package com.saurabh.mediadminapp.data.v1.repository

import com.saurabh.mediadminapp.data.v1.core.ApiResult
import com.saurabh.mediadminapp.data.v1.core.PageRequest
import com.saurabh.mediadminapp.data.v1.dto.Invoice
import com.saurabh.mediadminapp.data.v1.dto.Order
import com.saurabh.mediadminapp.data.v1.dto.OrderCreateRequest
import com.saurabh.mediadminapp.data.v1.dto.OrderEditRequest
import com.saurabh.mediadminapp.data.v1.dto.Sale

interface OrderRepository {
    /** GET orders (customers: own only; filter.userId admin-only). Both. */
    suspend fun list(filter: OrderFilter = OrderFilter(), page: PageRequest = PageRequest()): ApiResult<List<Order>>

    /** GET orders/{order_id} (includes `sales`; other users' orders -> NOT_FOUND). Both. */
    suspend fun get(orderId: String): ApiResult<Order>

    /** POST orders (multipart when [prescription] != null; idempotent via [idempotencyKey]). Customer. */
    suspend fun create(request: OrderCreateRequest, prescription: FilePart? = null, idempotencyKey: String): ApiResult<Order>

    /** PATCH orders/{order_id}: patient/doctor name while pending. Both (customer: own pending). */
    suspend fun edit(orderId: String, request: OrderEditRequest): ApiResult<Order>

    /** POST orders/{order_id}/approve (pending -> approved). Admin. */
    suspend fun approve(orderId: String): ApiResult<Order>

    /** POST orders/{order_id}/reject (pending/approved -> cancelled, with reason). Admin. */
    suspend fun reject(orderId: String, reason: String? = null): ApiResult<Order>

    /** POST orders/{order_id}/cancel (customer: own pending; admin: pending/approved; restocks). Both. */
    suspend fun cancel(orderId: String, reason: String? = null): ApiResult<Order>

    /** POST orders/{order_id}/verify-prescription (required before selling Rx orders). Admin. */
    suspend fun verifyPrescription(orderId: String, verified: Boolean = true): ApiResult<Order>

    /** POST orders/{order_id}/sell (approved -> sold, FEFO; idempotent via [idempotencyKey]). Admin. */
    suspend fun sell(orderId: String, idempotencyKey: String): ApiResult<Order>

    /** POST orders/{order_id}/return (sold -> returned, restocks batches). Admin. */
    suspend fun returnOrder(orderId: String, reason: String? = null): ApiResult<Order>

    /** GET orders/{order_id}/invoice (sold/returned orders only). Both. */
    suspend fun invoice(orderId: String): ApiResult<Invoice>

    /** POST orders/{order_id}/prescription (multipart jpeg/png/webp/pdf, max 5 MB). Both (customer: own). */
    suspend fun uploadPrescription(
        orderId: String,
        file: FilePart,
        doctorName: String? = null,
        patientName: String? = null
    ): ApiResult<Order>
}

interface SalesRepository {
    /** GET sales (ledger, one row per batch deducted). Admin. */
    suspend fun list(filter: SalesFilter = SalesFilter(), page: PageRequest = PageRequest()): ApiResult<List<Sale>>
}
