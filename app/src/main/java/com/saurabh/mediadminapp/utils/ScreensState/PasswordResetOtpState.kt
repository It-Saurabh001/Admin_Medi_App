package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.PasswordResetOtpResponse

data class PasswordResetOtpState(
    val isLoading: Boolean = false,
    val success: PasswordResetOtpResponse? = null,
    val error: String? = null
)
