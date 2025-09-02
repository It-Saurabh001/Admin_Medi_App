package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.GetDeleteSellHistoryResponse

data class GetDeleteSellHistoryState(
    val isLoading: Boolean = false,
    val success: GetDeleteSellHistoryResponse? = null,
    val error: String? = null

)
