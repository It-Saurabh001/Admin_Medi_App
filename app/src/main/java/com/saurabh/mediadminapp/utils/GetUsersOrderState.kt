package com.saurabh.mediadminapp.utils

import com.saurabh.mediadminapp.network.response.ApproveOrderResponse
import com.saurabh.mediadminapp.network.response.GetUsersOrdersResponse

data class GetUsersOrderState(
    val isLoading: Boolean = false,
    val success:  GetUsersOrdersResponse?= null,
    val error: String? = null
)
