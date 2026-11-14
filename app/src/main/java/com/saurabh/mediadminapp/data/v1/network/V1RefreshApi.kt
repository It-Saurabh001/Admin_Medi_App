package com.saurabh.mediadminapp.data.v1.network

import com.saurabh.mediadminapp.data.v1.core.ApiEnvelope
import com.saurabh.mediadminapp.data.v1.dto.RefreshData
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.POST

/** Refresh endpoint, served by the @V1AuthRetrofit instance (no authenticator). */
interface V1RefreshApi {
    @POST("auth/refresh")
    fun refresh(@Header("Authorization") bearerRefreshToken: String): Call<ApiEnvelope<RefreshData>>
}
