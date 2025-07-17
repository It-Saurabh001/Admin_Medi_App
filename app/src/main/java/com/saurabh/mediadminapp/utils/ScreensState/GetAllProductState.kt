package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.GetAllProductResponse

data class GetAllProductState (
    val isLoading: Boolean = false,
    val success:  GetAllProductResponse?= null,
    val error: String? = null
)