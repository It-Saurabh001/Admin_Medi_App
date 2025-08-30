package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.GetOrderByIdResponse
import com.saurabh.mediadminapp.network.response.GetSellHistoryResponse

data class GetSellHistoryState(
    val isLoading: Boolean = false,
    val success: GetSellHistoryResponse? = null,
    val error: String? = null
)
