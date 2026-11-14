package com.saurabh.mediadminapp.data.v1.error

sealed class AppError {
    data class Api(
        val httpStatus: Int,
        val code: ErrorCode,
        val message: String,
        val fields: Map<String, String>?
    ) : AppError()

    data object Network : AppError()
    data object Timeout : AppError()
    data class Unknown(val cause: Throwable?) : AppError()

    fun userMessage(): String = when (this) {
        is Api -> when {
            code == ErrorCode.INTERNAL_ERROR || message.isBlank() -> "Something went wrong. Please try again."
            else -> message
        }
        Network -> "No internet connection. Check your network and try again."
        Timeout -> "The request timed out. Please try again."
        is Unknown -> "Something went wrong. Please try again."
    }
}
