package com.saurabh.mediadminapp.data.v1.dto

import com.google.gson.annotations.SerializedName
import java.time.Instant

data class User(
    @SerializedName("user_id") val userId: String,
    val name: String,
    val email: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    val address: String?,
    @SerializedName("pin_code") val pinCode: String?,
    @SerializedName("image_url") val imageUrl: String?,
    val role: String,
    @SerializedName("is_approved") val isApproved: Boolean,
    @SerializedName("is_blocked") val isBlocked: Boolean,
    @SerializedName("is_deleted") val isDeleted: Boolean,
    @SerializedName("created_at") val createdAt: Instant?
)

/** PATCH users/{user_id} (admin). Password/status fields are rejected by the server. */
data class UserUpdateRequest(
    val name: String? = null,
    val email: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    val address: String? = null,
    @SerializedName("pin_code") val pinCode: String? = null
)