package com.saurabh.mediadminapp.network.response

data class RefreshTokenResponse(
    val access_token: String? = null,
    val status: Int
)