package com.saurabh.mediadminapp.utils

import com.saurabh.mediadminapp.network.response.GetAllUserResponse
import com.saurabh.mediadminapp.network.response.UpdateUserResponse

data class UpdateUserState(
    val isLoading: Boolean = false,
    val success: UpdateUserResponse? = null,
    val error: String? = null
)