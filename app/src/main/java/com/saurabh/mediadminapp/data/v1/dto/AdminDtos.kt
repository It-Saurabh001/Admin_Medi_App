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
    @SerializedName("created_at") val createdAt: Instant?,
    /** null on servers before admin signup; treat null as approved. */
    @SerializedName("is_approved") val isApproved: Boolean? = null,
    @SerializedName("is_owner") val isOwner: Boolean? = null,
    /** Google-linked admins cannot change their email (server returns 400 on email). */
    @SerializedName("google_linked") val googleLinked: Boolean? = null
)

/** POST admins/register (public). Server rejects role/is_approved/is_owner here. */
data class AdminRegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("signup_code") val signupCode: String? = null
)

/** POST admins/register/google (public). Name and email come from the Google token. */
data class AdminGoogleRegisterRequest(
    @SerializedName("id_token") val idToken: String,
    @SerializedName("signup_code") val signupCode: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null
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
