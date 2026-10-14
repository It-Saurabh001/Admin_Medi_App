package com.saurabh.mediadminapp.ui.auth.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saurabh.mediadminapp.ui.auth.data.FakeAuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed class SplashEvent {
    object NavigateToHome : SplashEvent()
    object NavigateToLogin : SplashEvent()
}

class SplashViewModel : ViewModel() {
    private val repository = FakeAuthRepository()
    private val _events = Channel<SplashEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            delay(1500L)
            val hasSession = repository.hasValidSession()
            _events.send(
                if (hasSession) SplashEvent.NavigateToHome
                else SplashEvent.NavigateToLogin
            )
        }
    }
}
