package com.saurabh.mediadminapp.ui.auth.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val credentialsError: String? = null,
    val emailTouched: Boolean = false,
    val passwordTouched: Boolean = false,
)
