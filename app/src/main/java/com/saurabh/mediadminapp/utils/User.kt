package com.saurabh.mediadminapp.utils

data class User(
    val id: Int,
    val user_id: String,
    val password: String,
    val date_of_account_creation: String,
    val isApproved: Boolean,
    val block: Boolean,
    val name: String,
    val address: String,
    val email: String,
    val phone_number: String,
    val pin_code: String
)
