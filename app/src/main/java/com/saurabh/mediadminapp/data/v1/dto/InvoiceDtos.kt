package com.saurabh.mediadminapp.data.v1.dto

import com.google.gson.annotations.SerializedName
import java.time.Instant
import java.time.LocalDate

data class Invoice(
    @SerializedName("invoice_number") val invoiceNumber: String,
    val currency: String,
    @SerializedName("issued_at") val issuedAt: Instant?,
    @SerializedName("generated_at") val generatedAt: Instant?,
    @SerializedName("prices_include_gst") val pricesIncludeGst: Boolean,
    val subtotal: Double,
    @SerializedName("gst_total") val gstTotal: Double,
    @SerializedName("grand_total") val grandTotal: Double,
    val items: List<InvoiceItem>,
    val order: Order,
    val user: InvoiceUser,
    /** Combined checkout invoice only (GET checkouts/{id}/invoice). */
    @SerializedName("checkout_id") val checkoutId: String? = null,
    val orders: List<Order>? = null
)

data class InvoiceItem(
    @SerializedName("product_id") val productId: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("batch_id") val batchId: String,
    @SerializedName("batch_no") val batchNo: String,
    @SerializedName("expiry_date") val expiryDate: LocalDate?,
    val quantity: Int,
    @SerializedName("unit_price") val unitPrice: Double,
    @SerializedName("gst_percent") val gstPercent: Double,
    @SerializedName("taxable_amount") val taxableAmount: Double,
    @SerializedName("gst_amount") val gstAmount: Double,
    @SerializedName("line_total") val lineTotal: Double
)

data class InvoiceUser(
    @SerializedName("user_id") val userId: String,
    val name: String,
    val email: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    val address: String?,
    @SerializedName("pin_code") val pinCode: String?
)
