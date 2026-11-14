package com.saurabh.mediadminapp.data.v1.repository

import com.saurabh.mediadminapp.data.v1.core.ApiResult
import com.saurabh.mediadminapp.data.v1.core.HealthResponse
import com.saurabh.mediadminapp.data.v1.dto.ApiIndex
import com.saurabh.mediadminapp.data.v1.dto.AppConfig
import com.saurabh.mediadminapp.data.v1.dto.LoginData
import com.saurabh.mediadminapp.data.v1.dto.LogoutData
import com.saurabh.mediadminapp.data.v1.dto.PasswordResetRequestData
import com.saurabh.mediadminapp.data.v1.dto.TokenData

interface AppConfigRepository {
    /** GET app-config (public, exempt from the version gate). Both. */
    suspend fun getAppConfig(): ApiResult<AppConfig>

    /** GET api/v1 index (public). Both. */
    suspend fun getApiIndex(): ApiResult<ApiIndex>

    /** GET /health liveness probe (public, not enveloped). Both. */
    suspend fun health(): ApiResult<HealthResponse>
}

interface AuthRepository {
    /** POST auth/login: emails an OTP. accountType = "admin" | "user". Both. */
    suspend fun login(email: String, password: String, accountType: String): ApiResult<LoginData>

    /** POST auth/verify-otp: returns tokens (the implementation persists the session). Both. */
    suspend fun verifyOtp(accountId: String, otp: String, accountType: String): ApiResult<TokenData>

    /** POST auth/logout: revokes access + refresh tokens, then clears the local session. Both. */
    suspend fun logout(): ApiResult<LogoutData>

    /** POST auth/password-reset/request: always 200 (no account enumeration). Both. */
    suspend fun requestPasswordReset(email: String, accountType: String): ApiResult<PasswordResetRequestData>

    /** POST auth/password-reset/confirm: OTP + new password (min 8 chars). Both. */
    suspend fun confirmPasswordReset(email: String, otp: String, newPassword: String, accountType: String): ApiResult<Unit?>

    // ── session helpers (local, no network) ──
    fun isLoggedIn(): Boolean
    fun currentRole(): String?
    fun currentAccountId(): String?
    fun currentAccountType(): String?

    /** Clears stored tokens without calling the server. */
    fun clearSession()
}
