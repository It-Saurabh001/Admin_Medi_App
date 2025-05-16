package com.saurabh.mediadminapp.network.response

data class UserItem(
    val address: String,
    val block: Boolean,
    val date_of_account_creation: String,
    val email: String,
    val id: Int,
    val isApproved: Boolean,
    val name: String,
    val password: String,
    val phone_number: String,
    val pin_code: String,
    val user_id: String
)