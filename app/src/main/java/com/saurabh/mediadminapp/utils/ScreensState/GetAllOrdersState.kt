package com.saurabh.mediadminapp.utils.ScreensState

import com.saurabh.mediadminapp.data.v1.core.Pagination
import com.saurabh.mediadminapp.data.v1.dto.*
import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.repository.PagedData

data class GetAllOrdersState(
    val isLoading: Boolean = false,
    val success: PagedData<Order>? = null,
    val error: String? = null,
    val pagination: Pagination? = null,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false
)