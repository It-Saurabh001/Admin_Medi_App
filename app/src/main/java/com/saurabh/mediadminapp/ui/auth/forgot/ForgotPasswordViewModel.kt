package com.saurabh.mediadminapp.ui.auth.forgot

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

data class ForgotUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val emailTouched: Boolean = false,
)

sealed class ForgotEvent {
    data class NavigateToReset(val email: String) : ForgotEvent()
    data class ShowSnackbar(val message: String) : ForgotEvent()
}

class ForgotPasswordViewModel : ViewModel() {

    private val repository = FakeAuthRepository()

    private val _uiState = MutableStateFlow(ForgotUiState())
    val uiState: StateFlow<ForgotUiState> = _uiState.asStateFlow()

    private val _events = Channel<ForgotEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onEmailChange(email: String) {
        _uiState.update { state ->
            state.copy(
                email = email,
                emailError = if (state.emailTouched) Validators.validateEmail(email) else null
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

    fun onSendCode() {
        val state = _uiState.value
        val emailErr = Validators.validateEmail(state.email)
        if (emailErr != null) {
            _uiState.update { it.copy(emailError = emailErr, emailTouched = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, emailError = null) }
            when (val result = repository.requestPasswordReset(state.email)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(ForgotEvent.NavigateToReset(state.email))
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    if (result.message == "EMAIL_NOT_FOUND") {
                        _uiState.update { it.copy(emailError = "This email isn't registered.") }
                    } else {
                        _events.send(ForgotEvent.ShowSnackbar("Couldn't send the code. Try again."))
                    }
                }
            }
        }
    }
}
