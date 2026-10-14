package com.saurabh.mediadminapp.ui.auth.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.mediadminapp.ui.auth.Validators
import com.saurabh.mediadminapp.ui.auth.data.AuthResult
import com.saurabh.mediadminapp.ui.auth.data.FakeAuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class LoginEvent {
    data class NavigateToOtp(val email: String) : LoginEvent()
    data class ShowSnackbar(val message: String) : LoginEvent()
}

class LoginViewModel(
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
) : ViewModel() {

    private val repository = FakeAuthRepository()

    private val _uiState = MutableStateFlow(LoginUiState(
        email = savedStateHandle.get<String>("email") ?: ""
    ))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onEmailChange(email: String) {
        savedStateHandle["email"] = email
        _uiState.update { state ->
            state.copy(
                email = email,
                emailError = if (state.emailTouched) Validators.validateEmail(email) else null,
                credentialsError = null
            )
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { state ->
            state.copy(
                password = password,
                passwordError = if (state.passwordTouched) Validators.validatePassword(password) else null,
                credentialsError = null
            )
        }
    }

    fun onEmailFocusLost() {
        _uiState.update { state ->
            state.copy(
                emailTouched = true,
                emailError = Validators.validateEmail(state.email)
            )
        }
    }

    fun onPasswordFocusLost() {
        _uiState.update { state ->
            state.copy(
                passwordTouched = true,
                passwordError = Validators.validatePassword(state.password)
            )
        }
    }

    fun onSubmit() {
        val state = _uiState.value
        val emailErr = Validators.validateEmail(state.email)
        val passErr = Validators.validatePassword(state.password)

        if (emailErr != null || passErr != null) {
            _uiState.update {
                it.copy(
                    emailError = emailErr,
                    passwordError = passErr,
                    emailTouched = true,
                    passwordTouched = true
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, credentialsError = null) }
            when (val result = repository.login(state.email, state.password)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(LoginEvent.NavigateToOtp(state.email))
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    if (result.message == "INVALID_CREDENTIALS") {
                        _uiState.update { it.copy(credentialsError = "Incorrect email or password. Try again.") }
                    } else {
                        _events.send(LoginEvent.ShowSnackbar("Network error. Please check your connection."))
                    }
                }
            }
        }
    }
}
