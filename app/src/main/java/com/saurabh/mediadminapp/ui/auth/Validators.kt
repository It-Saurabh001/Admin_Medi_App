package com.saurabh.mediadminapp.ui.auth

object Validators {

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Enter your email."
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email address."
        else -> null
    }

    fun validatePassword(password: String): String? = when {
        password.isBlank() -> "Enter your password."
        password.length < 8 -> "Password must be at least 8 characters."
        else -> null
    }

    fun validateNewPassword(password: String): String? = when {
        password.isBlank() -> "Enter your new password."
        password.length < 8 -> "Password must be at least 8 characters."
        !password.any { it.isDigit() } -> "Password must contain at least one number."
        !password.any { !it.isLetterOrDigit() } -> "Password must contain at least one symbol."
        else -> null
    }

    fun validateConfirmPassword(password: String, confirm: String): String? = when {
        confirm.isBlank() -> "Please confirm your password."
        confirm != password -> "Passwords don't match."
        else -> null
    }

    fun passwordHasMinLength(password: String) = password.length >= 8
    fun passwordHasNumber(password: String) = password.any { it.isDigit() }
    fun passwordHasSymbol(password: String) = password.any { !it.isLetterOrDigit() }

    fun maskEmail(email: String): String {
        val atIndex = email.indexOf('@')
        return if (atIndex > 1) {
            val firstChar = email[0]
            val domain = email.substring(atIndex)
            "$firstChar\u2022\u2022\u2022\u2022$domain"
        } else email
    }
}
