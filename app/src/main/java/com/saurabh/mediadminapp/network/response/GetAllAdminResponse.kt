package com.saurabh.mediadminapp.network.response

data class GetAllAdminResponse(
    val admins: List<Admin>,
    val message: String,
    val status: Int
)