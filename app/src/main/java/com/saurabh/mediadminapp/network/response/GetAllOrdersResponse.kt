package com.saurabh.mediadminapp.network.response

data class GetAllOrdersResponse(
    val message: String,
    val orders: List<Order>,
    val status: Int
)