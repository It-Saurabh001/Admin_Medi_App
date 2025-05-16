package com.saurabh.mediadminapp.common

sealed class ResultState<out T>{

    data class Success<out T>(val data: T) : ResultState<T>()
    data class Error<T>(val exception: Exception): ResultState<T>()
    object Loading : ResultState<Nothing>()
}