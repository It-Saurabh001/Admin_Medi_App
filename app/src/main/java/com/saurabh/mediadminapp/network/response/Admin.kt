package com.saurabh.mediadminapp.network.response

data class Admin(
    val admin_id: String,
    val date_of_account_creation: String,
    val email: String,
    val id: Int,
    val name: String,
    val password: String? = null,
    val phone_number: String? = null,
    val role: String
)