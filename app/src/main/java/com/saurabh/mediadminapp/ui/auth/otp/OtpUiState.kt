package com.saurabh.mediadminapp.ui.auth.otp

data class OtpUiState(
    val code: String = "",
    val secondsLeft: Int = 180, // 3 minutes
    val isVerifying: Boolean = false,
    val errorMessage: String? = null,
    val canResend: Boolean = false,
    val isResending: Boolean = false,
)
