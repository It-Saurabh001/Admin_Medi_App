package com.saurabh.mediadminapp.utils

import com.saurabh.mediadminapp.network.response.GetAllUserResponse


data class GetAllUserState(
    val isLoading: Boolean = false,
    val success: GetAllUserResponse? = null,
    val error: String? = null
)