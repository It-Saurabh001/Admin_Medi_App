package com.saurabh.mediadminapp.data.v1.api

import com.google.gson.JsonObject
import com.saurabh.mediadminapp.data.v1.core.ApiEnvelope
import com.saurabh.mediadminapp.data.v1.core.HealthReadyResponse
import com.saurabh.mediadminapp.data.v1.core.HealthResponse
import com.saurabh.mediadminapp.data.v1.dto.ApiIndex
import com.saurabh.mediadminapp.data.v1.dto.AppConfig
import com.saurabh.mediadminapp.data.v1.dto.EmailOtpRequest
import com.saurabh.mediadminapp.data.v1.dto.EmailVerifyData
import com.saurabh.mediadminapp.data.v1.dto.LoginData
import com.saurabh.mediadminapp.data.v1.dto.LoginRequest
import com.saurabh.mediadminapp.data.v1.dto.LogoutData
import com.saurabh.mediadminapp.data.v1.dto.LogoutRequest
import com.saurabh.mediadminapp.data.v1.dto.PasswordResetConfirmRequest
import com.saurabh.mediadminapp.data.v1.dto.PasswordResetRequest
import com.saurabh.mediadminapp.data.v1.dto.PasswordResetRequestData
import com.saurabh.mediadminapp.data.v1.dto.TokenData
import com.saurabh.mediadminapp.data.v1.dto.VerifyOtpRequest
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface MetaApi {
    /** GET api/v1 */
    @GET(".")
    suspend fun index(): Response<ApiEnvelope<ApiIndex>>

    @GET("app-config")
    suspend fun appConfig(): Response<ApiEnvelope<AppConfig>>

    /** Not enveloped (raw OpenAPI document). */
    @GET("openapi.json")
    suspend fun openApi(): Response<JsonObject>
}

/** Root-level probes: pass the full URL (e.g. "<host>/health"). Use with the @V1AuthRetrofit instance. */
interface HealthApi {
    @GET
    suspend fun health(@Url url: String): Response<HealthResponse>

    @GET
    suspend fun ready(@Url url: String): Response<HealthReadyResponse>
}

/** Token refresh lives in V1RefreshApi (auth client). Do NOT call refresh through this interface. */
interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<ApiEnvelope<LoginData>>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): Response<ApiEnvelope<TokenData>>

    @POST("auth/verify-otp")
    suspend fun verifyEmailOtp(@Body body: EmailOtpRequest): Response<ApiEnvelope<EmailVerifyData>>

    @POST("auth/resend-otp")
    suspend fun resendEmailOtp(@Body body: EmailOtpRequest): Response<ApiEnvelope<PasswordResetRequestData>>

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutRequest): Response<ApiEnvelope<LogoutData>>

    @POST("auth/password-reset/request")
    suspend fun passwordResetRequest(@Body body: PasswordResetRequest): Response<ApiEnvelope<PasswordResetRequestData>>

    @POST("auth/password-reset/confirm")
    suspend fun passwordResetConfirm(@Body body: PasswordResetConfirmRequest): Response<ApiEnvelope<Unit?>>
}

@Suppress("unused")
private typealias Parts = Map<String, RequestBody>
