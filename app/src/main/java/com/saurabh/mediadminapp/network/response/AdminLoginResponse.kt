package com.saurabh.mediadminapp.network.response

data class AdminLoginResponse(
    val admin_id: String? = null,
    val message: String? = null,
    val role: String? = null,
    val status: Int
)