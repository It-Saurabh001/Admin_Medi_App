package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.Admin

data class AdminState(
    val isLoading: Boolean = false,
    val success: Admin? = null,
    val error: String? = null
)
