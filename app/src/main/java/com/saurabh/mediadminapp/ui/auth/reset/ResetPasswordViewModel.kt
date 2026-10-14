package com.saurabh.mediadminapp.ui.auth.reset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

data class ResetUiState(
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val codeError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val newPasswordTouched: Boolean = false,
    val confirmTouched: Boolean = false,
    // Live password strength checklist
    val has8Chars: Boolean = false,
    val hasNumber: Boolean = false,
    val hasSymbol: Boolean = false,
)

sealed class ResetEvent {
    object NavigateToSuccess : ResetEvent()
    data class ShowSnackbar(val message: String) : ResetEvent()
}

class ResetPasswordViewModel(private val email: String) : ViewModel() {

    private val repository = FakeAuthRepository()

    private val _uiState = MutableStateFlow(ResetUiState())
    val uiState: StateFlow<ResetUiState> = _uiState.asStateFlow()

    private val _events = Channel<ResetEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onCodeChange(code: String) {
        if (code.length <= 6) {
            _uiState.update { it.copy(code = code, codeError = null) }
        }
    }

    fun onNewPasswordChange(pw: String) {
        _uiState.update { state ->
            state.copy(
                newPassword = pw,
                has8Chars = Validators.passwordHasMinLength(pw),
                hasNumber = Validators.passwordHasNumber(pw),
                hasSymbol = Validators.passwordHasSymbol(pw),
                newPasswordError = if (state.newPasswordTouched) Validators.validateNewPassword(pw) else null,
                confirmPasswordError = if (state.confirmTouched)
                    Validators.validateConfirmPassword(pw, state.confirmPassword) else null
            )
        }
    }

    fun onConfirmPasswordChange(confirm: String) {
        _uiState.update { state ->
            state.copy(
                confirmPassword = confirm,
                confirmPasswordError = if (state.confirmTouched)
                    Validators.validateConfirmPassword(state.newPassword, confirm) else null
            )
        }
    }

    fun onNewPasswordFocusLost() {
        _uiState.update { state ->
            state.copy(
                newPasswordTouched = true,
                newPasswordError = Validators.validateNewPassword(state.newPassword)
            )
        }
    }

    fun onConfirmFocusLost() {
        _uiState.update { state ->
            state.copy(
                confirmTouched = true,
                confirmPasswordError = Validators.validateConfirmPassword(state.newPassword, state.confirmPassword)
            )
        }
    }

    fun onReset() {
        val state = _uiState.value
        if (state.code.length != 6) {
            _uiState.update { it.copy(codeError = "Please enter the 6-digit code.") }
            return
        }
        val pwErr = Validators.validateNewPassword(state.newPassword)
        val cfErr = Validators.validateConfirmPassword(state.newPassword, state.confirmPassword)
        if (pwErr != null || cfErr != null) {
            _uiState.update {
                it.copy(
                    newPasswordError = pwErr,
                    confirmPasswordError = cfErr,
                    newPasswordTouched = true,
                    confirmTouched = true
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, codeError = null) }
            when (val result = repository.resetPassword(email, state.code, state.newPassword)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(ResetEvent.NavigateToSuccess)
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    if (result.message == "INVALID_OTP") {
                        _uiState.update { it.copy(codeError = "That code is invalid or expired.") }
                    } else {
                        _events.send(ResetEvent.ShowSnackbar("Something went wrong. Please try again."))
                    }
                }
            }
        }
    }

    class Factory(private val email: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ResetPasswordViewModel(email) as T
    }
}
