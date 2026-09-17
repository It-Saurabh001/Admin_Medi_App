package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.VerifyOtpResponse

data class VerifyOtpState(
    val isLoading: Boolean = false,
    val success: VerifyOtpResponse? = null,
    val error: String? = null
)
