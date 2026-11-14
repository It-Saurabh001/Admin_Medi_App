package com.saurabh.mediadminapp.data.v1.dto

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class DashboardSummary(
    val date: LocalDate?,
    val products: Int,
    @SerializedName("pending_orders") val pendingOrders: Int,
    @SerializedName("approved_orders") val approvedOrders: Int,
    @SerializedName("today_sales_count") val todaySalesCount: Int,
    @SerializedName("today_sales_total") val todaySalesTotal: Double,
    @SerializedName("low_stock") val lowStock: Int,
    @SerializedName("out_of_stock") val outOfStock: Int,
    @SerializedName("expired_batches") val expiredBatches: Int,
    @SerializedName("near_expiry_batches") val nearExpiryBatches: Int,
    @SerializedName("near_expiry_days") val nearExpiryDays: Int
)

/** GET reports/sales row. unverified: response.md shows no row example (data was empty). */
data class SalesReportRow(
    val key: String?, // unverified (day "YYYY-MM-DD" or product id; sort key is `key`)
    val quantity: Int?, // unverified
    val revenue: Double?, // unverified
    val orders: Int?, // unverified
    @SerializedName("product_name") val productName: String? // unverified
)

/** Parse from PagedEnvelope.summary with V1Gson.instance.fromJson(summary, SalesReportSummary::class.java). */
data class SalesReportSummary(
    val from: LocalDate?,
    val to: LocalDate?,
    @SerializedName("group_by") val groupBy: String?,
    val orders: Int,
    val quantity: Int,
    val revenue: Double
)

/** GET reports/top-products row. unverified: response.md shows no row example (data was empty). */
data class TopProduct(
    @SerializedName("product_id") val productId: String?, // unverified
    @SerializedName("product_name") val productName: String?, // unverified
    val quantity: Int?, // unverified
    val revenue: Double?, // unverified
    @SerializedName("image_url") val imageUrl: String? // unverified
)
