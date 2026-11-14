package com.saurabh.mediadminapp.data.v1.dto

import com.google.gson.annotations.SerializedName
import com.saurabh.mediadminapp.data.v1.core.OrderStatus
import java.time.Instant

/** One row per batch deducted for an order. */
data class Sale(
    @SerializedName("sale_id") val saleId: String,
    @SerializedName("order_id") val orderId: String,
    @SerializedName("order_status") val orderStatus: OrderStatus,
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_name") val userName: String,
    @SerializedName("product_id") val productId: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("batch_id") val batchId: String,
    @SerializedName("batch_no") val batchNo: String,
    val quantity: Int,
    val price: Double,
    @SerializedName("gst_percent") val gstPercent: Double,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("remaining_stock") val remainingStock: Int,
    @SerializedName("sold_by") val soldBy: String,
    @SerializedName("sold_at") val soldAt: Instant?
)
