package com.saurabh.mediadminapp.data.v1.core

import com.saurabh.mediadminapp.data.v1.error.AppError

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data object Empty : UiState<Nothing>
    data class Error(val error: AppError) : UiState<Nothing>
}
