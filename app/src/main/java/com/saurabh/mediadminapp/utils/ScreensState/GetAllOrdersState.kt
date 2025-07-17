package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.GetAllOrdersResponse

data class GetAllOrdersState (
    val isLoading: Boolean = false,
    val success:  GetAllOrdersResponse?= null,
    val error: String? = null
)