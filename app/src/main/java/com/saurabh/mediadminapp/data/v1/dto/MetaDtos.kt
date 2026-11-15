package com.saurabh.mediadminapp.data.v1.dto

import com.google.gson.annotations.SerializedName
import java.time.Instant

/** GET app-config */
data class AppConfig(
    @SerializedName("api_prefix") val apiPrefix: String,
    @SerializedName("api_version") val apiVersion: String,
    @SerializedName("docs_enabled") val docsEnabled: Boolean,
    val enums: AppConfigEnums,
    @SerializedName("error_codes") val errorCodes: List<String>,
    @SerializedName("min_app_version") val minAppVersion: String?,
    val otp: AppConfigOtp,
    val pagination: AppConfigPagination,
    @SerializedName("server_time") val serverTime: Instant?,
    val uploads: AppConfigUploads,
    /** null on servers before admin signup. */
    @SerializedName("admin_signup") val adminSignup: AppConfigAdminSignup? = null
)

data class AppConfigAdminSignup(
    val enabled: Boolean,
    @SerializedName("code_required") val codeRequired: Boolean
)

data class AppConfigEnums(
    @SerializedName("account_types") val accountTypes: List<String>,
    @SerializedName("device_platforms") val devicePlatforms: List<String>,
    @SerializedName("dosage_forms") val dosageForms: List<String>,
    @SerializedName("order_statuses") val orderStatuses: List<String>,
    @SerializedName("prescription_schedules") val prescriptionSchedules: List<String>,
    @SerializedName("schedule_types") val scheduleTypes: List<String>,
    @SerializedName("stock_movement_reasons") val stockMovementReasons: List<String>,
    val units: List<String>
)

data class AppConfigOtp(
    @SerializedName("expires_in") val expiresIn: Int,
    val length: Int,
    @SerializedName("max_attempts") val maxAttempts: Int
)

data class AppConfigPagination(
    @SerializedName("default_limit") val defaultLimit: Int,
    @SerializedName("max_limit") val maxLimit: Int
)

data class AppConfigUploads(
    @SerializedName("allowed_mime_types") val allowedMimeTypes: List<String>,
    @SerializedName("image_mime_types") val imageMimeTypes: List<String>,
    @SerializedName("max_request_bytes") val maxRequestBytes: Long,
    @SerializedName("max_upload_mb") val maxUploadMb: Int
)

/** GET api/v1 */
data class ApiIndex(
    val name: String,
    val version: String,
    @SerializedName("api_version") val apiVersion: String,
    val links: List<ApiLink>
)

data class ApiLink(
    val href: String,
    val methods: List<String>,
    val rel: String
)
