package com.saurabh.mediadminapp.network.response

data class SellHistory(
    val Order_id: String,
    val Sell_id: String,
    val date_of_sell: String,
    val id: Int,
    val isApproved: Boolean,
    val price: Double,
    val product_id: String,
    val product_name: String,
    val quantity: Int,
    val remaining_stock: Int,
    val total_amount: Double,
    val user_id: String,
    val user_name: String
)