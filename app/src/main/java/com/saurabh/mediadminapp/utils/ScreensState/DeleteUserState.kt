package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.network.response.DeleteUserResponse

data class DeleteUserState(
    val isLoading: Boolean = false,
    val success: DeleteUserResponse? = null,
    val error: String? = null
)