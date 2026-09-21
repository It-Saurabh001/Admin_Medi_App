package com.saurabh.mediadminapp.utils

/**
 * Single-shot side-effect events for Compose UI.
 * Emitted via Channel<UiEvent> (capacity = BUFFERED) and consumed with
 * LaunchedEffect + channel.receiveAsFlow() so each event fires exactly once,
 * even across recompositions.
 */
sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
}
