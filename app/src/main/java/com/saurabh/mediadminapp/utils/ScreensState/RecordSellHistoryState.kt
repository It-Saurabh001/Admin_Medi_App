package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.GetRecordSellHistoryResoponse

data class RecordSellHistoryState(
    val isLoading: Boolean = false,
    val success: GetRecordSellHistoryResoponse? = null,
    val error: String? = null
)
