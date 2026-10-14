package com.saurabh.mediadminapp.ui.auth.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.saurabh.mediadminapp.ui.auth.data.AuthResult
import com.saurabh.mediadminapp.ui.auth.data.FakeAuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class OtpEvent {
    object NavigateToHome : OtpEvent()
    data class ShowSnackbar(val message: String) : OtpEvent()
}

class OtpViewModel(private val email: String) : ViewModel() {

    private val repository = FakeAuthRepository()

    private val _uiState = MutableStateFlow(OtpUiState())
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    private val _events = Channel<OtpEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var timerJob: Job? = null

    init {
        startTimer()
    }

    private fun startTimer(seconds: Int = 180) {
        timerJob?.cancel()
        _uiState.update { it.copy(secondsLeft = seconds, canResend = false) }
        timerJob = viewModelScope.launch {
            for (remaining in seconds downTo 0) {
                _uiState.update { it.copy(secondsLeft = remaining) }
                if (remaining == 0) {
                    _uiState.update { it.copy(canResend = true) }
                    break
                }
                delay(1000L)
            }
        }
    }

    fun onCodeChange(code: String) {
        if (code.length <= 6) {
            _uiState.update { it.copy(code = code, errorMessage = null) }
        }
    }

    fun onVerify() {
        val state = _uiState.value
        if (state.code.length != 6 || state.secondsLeft == 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, errorMessage = null) }
            when (val result = repository.verifyOtp(email, state.code)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isVerifying = false) }
                    _events.send(OtpEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isVerifying = false,
                            errorMessage = "That code isn't right. Check and try again."
                        )
                    }
                }
            }
        }
    }

    fun onResend() {
        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true) }
            when (repository.resendOtp(email)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isResending = false, code = "") }
                    startTimer()
                    _events.send(OtpEvent.ShowSnackbar("New code sent."))
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isResending = false) }
                    _events.send(OtpEvent.ShowSnackbar("Couldn't send code. Check your connection and try again."))
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    class Factory(private val email: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = OtpViewModel(email) as T
    }
}
