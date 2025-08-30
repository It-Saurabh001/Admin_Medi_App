package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.GetProductSellHistoryResponse
import com.saurabh.mediadminapp.network.response.GetSellHistoryResponse

data class GetProductSellHistoryState(
    val isLoading: Boolean = false,
    val success: GetProductSellHistoryResponse? = null,
    val error: String? = null
)
