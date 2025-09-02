package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.GetSpecificProductResponse
import com.saurabh.mediadminapp.network.response.GetUserSellHistoryResponse

data class GetUserSellHistoryState(
    val isLoading: Boolean = false,
    val success: GetUserSellHistoryResponse? = null,
    val error: String? = null

)
