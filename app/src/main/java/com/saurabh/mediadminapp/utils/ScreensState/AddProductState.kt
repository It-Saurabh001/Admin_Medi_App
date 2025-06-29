package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.GetAddProductResponse

data class AddProductState(
    val isLoading: Boolean = false,
    val success: GetAddProductResponse? = null,
    val error: String? = null
)