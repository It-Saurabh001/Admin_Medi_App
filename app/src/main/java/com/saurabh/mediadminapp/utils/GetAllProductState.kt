package com.saurabh.mediadminapp.utils

import com.saurabh.mediadminapp.network.response.GetAllProductResponse
import com.saurabh.mediadminapp.network.response.GetAllUserResponse

data class GetAllProductState (
    val isLoading: Boolean = false,
    val success:  GetAllProductResponse?= null,
    val error: String? = null
)