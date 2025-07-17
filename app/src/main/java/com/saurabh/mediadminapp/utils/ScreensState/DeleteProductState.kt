package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.DeleteProductResponse

data class DeleteProductState(
    val isLoading: Boolean = false,
    val success: DeleteProductResponse? = null,
    val error: String? = null
)