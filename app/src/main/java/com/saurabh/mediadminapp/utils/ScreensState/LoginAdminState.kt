package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.AdminLoginResponse

data class LoginAdminState(
    val isLoading: Boolean = false,
    val success: AdminLoginResponse? = null,
    val error: String? = null
)
