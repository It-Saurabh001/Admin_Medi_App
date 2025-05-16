package com.saurabh.mediadminapp.network.response

data class GetAllUserResponse(
    val message: String,
    val status: Int,
    val users: List<UserItem>
)