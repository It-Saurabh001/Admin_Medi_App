package com.saurabh.mediadminapp.utils

import com.saurabh.mediadminapp.network.response.ApproveOrderResponse
import com.saurabh.mediadminapp.network.response.DeleteOrderResponse

data class ApproveOrderState(
    val isLoading: Boolean = false,
    val success:  ApproveOrderResponse?= null,
    val error: String? = null
)
