package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.DeleteOrderResponse

data class DeleteOrderState (
    val isLoading: Boolean = false,
    val success:  DeleteOrderResponse?= null,
    val error: String? = null
)