package com.saurabh.mediadminapp.utils

import com.saurabh.mediadminapp.network.response.DeleteUserResponse
import com.saurabh.mediadminapp.network.response.IsApproveUserResponse

data class DeleteUserState(
    val isLoading: Boolean = false,
    val success: DeleteUserResponse? = null,
    val error: String? = null
)
