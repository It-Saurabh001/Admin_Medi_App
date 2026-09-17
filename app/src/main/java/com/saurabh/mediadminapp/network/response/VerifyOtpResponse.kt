package com.saurabh.mediadminapp.network.response

data class VerifyOtpResponse(
    val access_token: String? = null,
    val refresh_token: String? = null,
    val role: String? = null,
    val message: String? = null,
    val status: Int
)