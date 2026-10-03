package com.saurabh.mediadminapp.network.response

data class ApiErrorResponse(
    val status: Int? = null,
    val message: String? = null,
    val msg: String? = null
) {
    val displayMessage: String?
        get() = message?.takeIf { it.isNotBlank() } ?: msg?.takeIf { it.isNotBlank() }
}
