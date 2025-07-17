package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.GetUsersOrdersResponse

data class GetUsersOrderState(
    val isLoading: Boolean = false,
    val success:  GetUsersOrdersResponse?= null,
    val error: String? = null
)