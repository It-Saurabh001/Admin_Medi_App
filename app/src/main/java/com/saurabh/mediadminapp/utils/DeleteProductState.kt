package com.saurabh.mediadminapp.utils

import com.saurabh.mediadminapp.network.response.DeleteProductResponse
import com.saurabh.mediadminapp.network.response.GetAllUserResponse

data class DeleteProductState(
    val isLoading: Boolean = false,
    val success: DeleteProductResponse? = null,
    val error: String? = null
)
