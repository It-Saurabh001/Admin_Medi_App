package com.saurabh.mediadminapp.network.response

data class GetUserSellHistoryResponse(
    val message: String,
    val sell_history: List<SellHistoryXX>,
    val status: Int
)