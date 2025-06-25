package com.saurabh.mediadminapp.utils

import com.saurabh.mediadminapp.network.response.GetSpecificProductResponse

data class GetSpecificProductState(
    val isLoading: Boolean = false,
    val success: GetSpecificProductResponse? = null,
    val error: String? = null
)
