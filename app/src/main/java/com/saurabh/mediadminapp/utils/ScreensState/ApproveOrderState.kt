package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.ApproveOrderResponse

data class ApproveOrderState(
    val isLoading: Boolean = false,
    val success:  ApproveOrderResponse?= null,
    val error: String? = null
)