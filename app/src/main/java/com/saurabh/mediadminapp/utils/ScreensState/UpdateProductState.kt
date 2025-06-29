package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.UpdateProductResponse

data class UpdateProductState(
    val isLoading: Boolean = false,
    val success: UpdateProductResponse? = null,
    val error: String? = null
)