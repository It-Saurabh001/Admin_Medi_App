package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.GetAllAdminResponse
import com.saurabh.mediadminapp.network.response.GetAllOrdersResponse

data class GetAllAdminState(
    val isLoading: Boolean = false,
    val success:  GetAllAdminResponse?= null,
    val error: String? = null
)
