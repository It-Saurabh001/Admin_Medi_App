package com.saurabh.mediadminapp.network.response

data class OrderX(
    val category: String,
    val date_of_order_creation: String,
    val id: Int,
    val isApproved: Boolean,
    val message: String,
    val order_id: String,
    val price: Double,
    val product_id: String,
    val product_name: String,
    val quantity: Int,
    val total_amount: Double,
    val user_id: String,
    val user_name: String
)