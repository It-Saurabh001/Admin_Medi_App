package com.saurabh.mediadminapp.data.v1.dto

import com.google.gson.annotations.SerializedName
import com.saurabh.mediadminapp.data.v1.core.OrderStatus
import java.time.Instant

/**
 * Order. `sales` is present on GET orders/{id} and POST sell; `restocked` on cancel/reject/return;
 * `notRestocked` on return. Those are null otherwise.
 */
data class Order(
    @SerializedName("order_id") val orderId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_name") val userName: String,
    @SerializedName("product_id") val productId: String,
    @SerializedName("product_name") val productName: String,
    val category: String,
    @SerializedName("image_url") val imageUrl: String?,
    val quantity: Int,
    val price: Double,
    @SerializedName("total_amount") val totalAmount: Double,
    val status: OrderStatus,
    val message: String,
    @SerializedName("doctor_name") val doctorName: String,
    @SerializedName("patient_name") val patientName: String,
    @SerializedName("prescription_url") val prescriptionUrl: String?,
    @SerializedName("prescription_verified") val prescriptionVerified: Boolean,
    @SerializedName("requires_prescription") val requiresPrescription: Boolean,
    @SerializedName("cancel_reason") val cancelReason: String,
    @SerializedName("created_at") val createdAt: Instant?,
    @SerializedName("updated_at") val updatedAt: Instant?,
    val sales: List<Sale>?,
    val restocked: List<RestockedLine>?,
    @SerializedName("not_restocked") val notRestocked: List<RestockedLine>?, // unverified: element shape (empty in examples)
    /** Set when the order was placed via POST checkouts (multi-item); null otherwise. */
    @SerializedName("checkout_id") val checkoutId: String? = null
)

data class RestockedLine(
    @SerializedName("batch_id") val batchId: String,
    @SerializedName("batch_no") val batchNo: String,
    val qty: Int
)

/** PATCH orders/{order_id} (pending only). */
data class OrderEditRequest(
    @SerializedName("patient_name") val patientName: String? = null,
    @SerializedName("doctor_name") val doctorName: String? = null
)

/** POST orders/{id}/cancel */
data class CancelOrderRequest(
    @SerializedName("cancel_reason") val cancelReason: String? = null
)

/** POST orders/{id}/reject (server stores "Rejected: <reason>") */
data class RejectOrderRequest(
    val reason: String? = null
)

/** POST orders/{id}/return */
data class ReturnOrderRequest(
    val reason: String? = null
)

/** POST orders/{id}/verify-prescription */
data class VerifyPrescriptionRequest(
    val verified: Boolean = true
)
