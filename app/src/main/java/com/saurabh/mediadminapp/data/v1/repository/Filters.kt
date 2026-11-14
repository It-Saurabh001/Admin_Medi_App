package com.saurabh.mediadminapp.data.v1.repository

import com.saurabh.mediadminapp.data.v1.core.OrderStatus
import java.time.Instant
import java.time.LocalDate

/** Inclusive date range; maps to the `from` / `to` query params (YYYY-MM-DD). */
data class DateRange(val from: LocalDate? = null, val to: LocalDate? = null)

/** Image upload without OkHttp types (jpeg/png/webp). */
class ImagePart(val fileName: String, val mimeType: String, val bytes: ByteArray)

/** GET products query params. */
data class ProductFilter(
    val q: String? = null,
    val category: String? = null,
    val scheduleType: String? = null,
    val requiresPrescription: Boolean? = null,
    val isActive: Boolean? = null,
    val inStock: Boolean? = null,
    val updatedSince: Instant? = null
)

/** GET orders / users/{id}/orders query params (`userId` is admin-only and ignored by ordersOf). */
data class OrderFilter(
    val status: OrderStatus? = null,
    val productId: String? = null,
    val userId: String? = null,
    val dateRange: DateRange = DateRange(),
    val updatedSince: Instant? = null
)

/** GET sales query params. */
data class SalesFilter(
    val dateRange: DateRange = DateRange(),
    val productId: String? = null,
    val userId: String? = null,
    val orderId: String? = null,
    val batchId: String? = null,
    val updatedSince: Instant? = null
)

/** GET users query params. */
data class UserFilter(
    val q: String? = null,
    val isApproved: Boolean? = null,
    val isBlocked: Boolean? = null,
    val includeDeleted: Boolean? = null,
    val updatedSince: Instant? = null
)

/** GET stock-movements query params (`reason`: PURCHASE|SALE|ADJUSTMENT|EXPIRED_WRITEOFF|RETURN|CANCEL_RESTOCK). */
data class StockMovementFilter(
    val productId: String? = null,
    val batchId: String? = null,
    val reason: String? = null,
    val performedBy: String? = null,
    val dateRange: DateRange = DateRange()
)

/** GET audit-logs query params. */
data class AuditFilter(
    val entity: String? = null,
    val entityId: String? = null,
    val actorId: String? = null,
    val action: String? = null,
    val dateRange: DateRange = DateRange()
)
