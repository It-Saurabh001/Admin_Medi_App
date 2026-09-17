package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.PasswordResetResponse

data class PasswordResetState(
    val isLoading: Boolean = false,
    val success: PasswordResetResponse? = null,
    val error: String? = null
)
