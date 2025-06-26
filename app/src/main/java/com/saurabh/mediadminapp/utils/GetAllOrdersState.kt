package com.saurabh.mediadminapp.utils

import com.saurabh.mediadminapp.network.response.GetAllOrdersResponse
import com.saurabh.mediadminapp.network.response.GetAllProductResponse


data class GetAllOrdersState (
    val isLoading: Boolean = false,
    val success:  GetAllOrdersResponse?= null,
    val error: String? = null
)