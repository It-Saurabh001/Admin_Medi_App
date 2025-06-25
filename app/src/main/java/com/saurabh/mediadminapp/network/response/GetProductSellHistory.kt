package com.saurabh.mediadminapp.network.response

data class GetProductSellHistory(
    val message: String,
    val sell_history: List<SellHistoryX>,
    val status: Int
)