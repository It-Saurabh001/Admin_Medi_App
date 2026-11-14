package com.saurabh.mediadminapp.data.v1.dto

import com.google.gson.annotations.SerializedName
import java.time.Instant

data class Product(
    @SerializedName("product_id") val productId: String,
    val name: String,
    @SerializedName("generic_name") val genericName: String,
    @SerializedName("brand_name") val brandName: String,
    val manufacturer: String,
    val category: String,
    @SerializedName("dosage_form") val dosageForm: String,
    val strength: String,
    @SerializedName("pack_size") val packSize: String,
    val unit: String,
    @SerializedName("hsn_code") val hsnCode: String,
    @SerializedName("gst_percent") val gstPercent: Double,
    val price: Double,
    val mrp: Double,
    /** null for customers. */
    @SerializedName("purchase_price") val purchasePrice: Double?,
    @SerializedName("schedule_type") val scheduleType: String, // OTC|H|H1|X|NARCOTIC
    @SerializedName("requires_prescription") val requiresPrescription: Boolean,
    @SerializedName("storage_conditions") val storageConditions: String,
    @SerializedName("reorder_level") val reorderLevel: Int,
    val barcode: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("is_active") val isActive: Boolean,
    val stock: Int,
    @SerializedName("sellable_stock") val sellableStock: Int,
    @SerializedName("in_stock") val inStock: Boolean,
    @SerializedName("created_at") val createdAt: Instant?,
    @SerializedName("updated_at") val updatedAt: Instant?
)

data class ProductCategory(
    val name: String,
    @SerializedName("product_count") val productCount: Int
)

/** POST products (stock starts at 0). Image goes as multipart part, not here. */
data class ProductCreateRequest(
    val name: String,
    val price: Double,
    @SerializedName("generic_name") val genericName: String? = null,
    @SerializedName("brand_name") val brandName: String? = null,
    val manufacturer: String? = null,
    val category: String? = null,
    @SerializedName("dosage_form") val dosageForm: String? = null,
    val strength: String? = null,
    @SerializedName("pack_size") val packSize: String? = null,
    val unit: String? = null,
    @SerializedName("hsn_code") val hsnCode: String? = null,
    @SerializedName("gst_percent") val gstPercent: Double? = null,
    val mrp: Double? = null,
    @SerializedName("purchase_price") val purchasePrice: Double? = null,
    @SerializedName("schedule_type") val scheduleType: String? = null,
    @SerializedName("requires_prescription") val requiresPrescription: Boolean? = null,
    @SerializedName("storage_conditions") val storageConditions: String? = null,
    @SerializedName("reorder_level") val reorderLevel: Int? = null,
    val barcode: String? = null
)

/** PATCH products/{product_id}: only non-null fields are sent. Stock is not editable here (use stock/adjust). */
data class ProductUpdateRequest(
    val name: String? = null,
    val price: Double? = null,
    @SerializedName("generic_name") val genericName: String? = null,
    @SerializedName("brand_name") val brandName: String? = null,
    val manufacturer: String? = null,
    val category: String? = null,
    @SerializedName("dosage_form") val dosageForm: String? = null,
    val strength: String? = null,
    @SerializedName("pack_size") val packSize: String? = null,
    val unit: String? = null,
    @SerializedName("hsn_code") val hsnCode: String? = null,
    @SerializedName("gst_percent") val gstPercent: Double? = null,
    val mrp: Double? = null,
    @SerializedName("purchase_price") val purchasePrice: Double? = null,
    @SerializedName("schedule_type") val scheduleType: String? = null,
    @SerializedName("requires_prescription") val requiresPrescription: Boolean? = null,
    @SerializedName("storage_conditions") val storageConditions: String? = null,
    @SerializedName("reorder_level") val reorderLevel: Int? = null,
    val barcode: String? = null
)
