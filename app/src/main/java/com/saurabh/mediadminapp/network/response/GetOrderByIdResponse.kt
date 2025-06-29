package com.saurabh.mediadminapp.network.response

data class GetOrderByIdResponse(
    val message: String,
    val order: OrderXX,
    val status: Int
)