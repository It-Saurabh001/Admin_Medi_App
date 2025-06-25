package com.saurabh.mediadminapp.network.response

data class GetUsersOrdersResponse(
    val message: String,
    val order: List<OrderX>,
    val status: Int
)