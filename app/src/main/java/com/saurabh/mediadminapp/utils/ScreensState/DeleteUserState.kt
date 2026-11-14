package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.data.v1.core.Pagination
import com.saurabh.mediadminapp.data.v1.dto.*
import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.repository.PagedData

data class DeleteUserState(
    val isLoading: Boolean = false,
    val success: User? = null,
    val error: String? = null
)