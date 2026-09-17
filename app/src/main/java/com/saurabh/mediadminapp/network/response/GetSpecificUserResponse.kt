package com.saurabh.mediadminapp.network.response

data class GetSpecificUserResponse(
    val message: String,
    val status: Int,
    val user: UserItem
)