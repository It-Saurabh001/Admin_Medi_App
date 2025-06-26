package com.saurabh.mediadminapp.utils

import com.saurabh.mediadminapp.network.response.DeleteOrderResponse
import com.saurabh.mediadminapp.network.response.GetAllProductResponse



data class DeleteOrderState (
    val isLoading: Boolean = false,
    val success:  DeleteOrderResponse?= null,
    val error: String? = null
)