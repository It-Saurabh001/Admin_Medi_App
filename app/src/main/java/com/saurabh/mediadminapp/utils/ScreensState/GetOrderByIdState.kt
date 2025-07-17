package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.GetOrderByIdResponse

data class GetOrderByIdState(
    val isLoading: Boolean = false,
    val success: GetOrderByIdResponse? = null,
    val error: String? = null
)