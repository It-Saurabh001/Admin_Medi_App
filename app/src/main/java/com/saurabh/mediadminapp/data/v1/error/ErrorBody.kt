package com.saurabh.mediadminapp.data.v1.error

data class ErrorBody(
    val status: Int? = null,
    val message: String? = null,
    val code: ErrorCode? = null,
    val fields: Map<String, String>? = null
)
