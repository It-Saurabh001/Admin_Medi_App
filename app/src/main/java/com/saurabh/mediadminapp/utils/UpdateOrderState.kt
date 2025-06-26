package com.saurabh.mediadminapp.utils

import com.saurabh.mediadminapp.network.response.GetAllOrdersResponse
import com.saurabh.mediadminapp.network.response.UpdateOrderResponse


data class UpdateOrderState (
    val isLoading: Boolean = false,
    val success:  UpdateOrderResponse?= null,
    val error: String? = null
)