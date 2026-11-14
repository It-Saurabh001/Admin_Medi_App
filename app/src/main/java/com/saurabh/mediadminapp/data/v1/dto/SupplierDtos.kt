package com.saurabh.mediadminapp.data.v1.dto

import com.google.gson.annotations.SerializedName
import java.time.Instant

data class Supplier(
    @SerializedName("supplier_id") val supplierId: String,
    val name: String,
    @SerializedName("contact_phone") val contactPhone: String?, // unverified: null possible
    val email: String?, // unverified: null possible
    @SerializedName("gst_number") val gstNumber: String?, // unverified: null possible
    val address: String?, // unverified: null possible
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: Instant?,
    @SerializedName("updated_at") val updatedAt: Instant?
)

data class SupplierCreateRequest(
    val name: String,
    @SerializedName("contact_phone") val contactPhone: String? = null,
    val email: String? = null,
    @SerializedName("gst_number") val gstNumber: String? = null,
    val address: String? = null
)

data class SupplierUpdateRequest(
    val name: String? = null,
    @SerializedName("contact_phone") val contactPhone: String? = null,
    val email: String? = null,
    @SerializedName("gst_number") val gstNumber: String? = null,
    val address: String? = null
)
