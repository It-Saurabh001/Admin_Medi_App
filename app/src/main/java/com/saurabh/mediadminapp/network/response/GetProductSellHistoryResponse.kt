package com.saurabh.mediadminapp.network.response

data class GetProductSellHistoryResponse(
    val message: String,
    val sell_history: List<SellHistory>,
    val status: Int
)