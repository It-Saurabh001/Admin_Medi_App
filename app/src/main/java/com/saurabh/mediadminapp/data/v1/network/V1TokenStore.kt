package com.saurabh.mediadminapp.data.v1.network

import com.saurabh.mediadminapp.data.v1.core.ApiEnvelope
import com.saurabh.mediadminapp.data.v1.dto.RefreshData
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.POST

/** Minimal token storage used by v1 interceptors (implemented by TokenManager). */
interface V1TokenStore {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun saveSession(
        accessToken: String,
        refreshToken: String,
        role: String?,
        accountId: String?,
        accountType: String?,
        expiresInSeconds: Long?
    )
    fun clear()
}

/** Refresh endpoint, served by the v1 auth client (no authenticator). */
interface V1RefreshApi {
    @POST("auth/refresh")
    fun refresh(@Header("Authorization") bearerRefreshToken: String): Call<ApiEnvelope<RefreshData>>
}
