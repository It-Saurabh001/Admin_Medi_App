package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.CreateAdminResponse

data class CreateAdminState(
    val isLoading: Boolean = false,
    val success: CreateAdminResponse? = null,
    val error: String? = null
)
