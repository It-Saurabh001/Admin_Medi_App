package com.saurabh.mediadminapp.data.v1.dto

import com.google.gson.annotations.SerializedName
import java.time.Instant

data class Device(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("user_id") val userId: String?, // unverified: value for admin accounts
    val platform: String,
    @SerializedName("app_version") val appVersion: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: Instant?,
    @SerializedName("updated_at") val updatedAt: Instant?
)

/** POST devices. platform: android | ios. */
data class DeviceRegisterRequest(
    val token: String,
    val platform: String = "android",
    @SerializedName("app_version") val appVersion: String? = null
)
