package com.saurabh.mediadminapp.data.v1.dto

import com.google.gson.annotations.SerializedName

/** POST auth/login */
data class LoginRequest(
    val email: String,
    val password: String,
    @SerializedName("account_type") val accountType: String = "user" // "user" | "admin"
)

data class LoginData(
    @SerializedName("account_id") val accountId: String,
    @SerializedName("account_type") val accountType: String,
    @SerializedName("otp_required") val otpRequired: Boolean,
    @SerializedName("otp_expires_in") val otpExpiresIn: Int
)

/** POST auth/verify-otp */
data class VerifyOtpRequest(
    @SerializedName("account_id") val accountId: String,
    val otp: String,
    @SerializedName("account_type") val accountType: String = "user"
)

/** POST auth/verify-otp (signup email verification) and auth/resend-otp. */
data class EmailOtpRequest(
    val email: String,
    val otp: String? = null,
    @SerializedName("account_type") val accountType: String = "admin"
)

data class EmailVerifyData(
    @SerializedName("account_id") val accountId: String? = null,
    @SerializedName("email_verified") val emailVerified: Boolean = false
)

data class TokenData(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int,
    val role: String,
    @SerializedName("account_type") val accountType: String,
    @SerializedName("account_id") val accountId: String
)

/** POST auth/refresh (same shape as [TokenData]). */
data class RefreshData(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int,
    val role: String,
    @SerializedName("account_type") val accountType: String,
    @SerializedName("account_id") val accountId: String
)

/** POST auth/logout */
data class LogoutRequest(
    @SerializedName("refresh_token") val refreshToken: String? = null
)

data class LogoutData(
    val revoked: List<String>
)

/** POST auth/password-reset/request */
data class PasswordResetRequest(
    val email: String,
    @SerializedName("account_type") val accountType: String = "user"
)

data class PasswordResetRequestData(
    @SerializedName("otp_expires_in") val otpExpiresIn: Int
)

/** POST auth/password-reset/confirm (response data is null: use ApiEnvelope<Unit?>). */
data class PasswordResetConfirmRequest(
    val email: String,
    val otp: String,
    @SerializedName("new_password") val newPassword: String,
    @SerializedName("account_type") val accountType: String = "user"
)
