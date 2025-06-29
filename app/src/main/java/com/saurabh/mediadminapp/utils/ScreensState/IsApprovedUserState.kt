package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.IsApproveUserResponse

data class IsApprovedUserState(
    val isLoading: Boolean = false,
    val success: IsApproveUserResponse? = null,
    val error: String? = null
)