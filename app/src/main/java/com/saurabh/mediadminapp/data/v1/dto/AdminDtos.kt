package com.saurabh.mediadminapp.data.v1.dto

import com.google.gson.annotations.SerializedName
import java.time.Instant

data class Admin(
    @SerializedName("admin_id") val adminId: String,
    val name: String,
    val email: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("image_url") val imageUrl: String?,
    val role: String, // admin | disabled | deleted
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: Instant?
)

data class AdminCreateRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("phone_number") val phoneNumber: String? = null
)

/** PATCH admins/{admin_id}. `role` = "disabled" demotes. email/password: owner only + currentPassword. */
data class AdminUpdateRequest(
    val name: String? = null,
    val email: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    val password: String? = null,
    val role: String? = null,
    @SerializedName("current_password") val currentPassword: String? = null
)
