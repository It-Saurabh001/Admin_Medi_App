package com.saurabh.mediadminapp.utils.utilityFunctions

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager

/**
 * Wraps [content] in a full-screen Box that dismisses the soft keyboard
 * when the user taps anywhere outside a focused text field.
 *
 * FIX: Previously used `awaitPointerEvent()` which fired on pointer-DOWN,
 * racing with Compose's own focus-gain assignment (also pointer-DOWN) and
 * causing text fields to feel unresponsive on first tap.
 *
 * Now uses `detectTapGestures(onTap = ...)` which fires on pointer-UP
 * (after release), safely after Compose has processed focus assignment.
 * This means the keyboard dismisses on tap-release — the correct UX behaviour.
 */
@Composable
fun DismissKeyboardOnTapScreen(content: @Composable () -> Unit) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        // Fires on pointer-UP — after Compose focus assignment is complete.
                        // This prevents the race condition where clearFocus() cancelled
                        // the focus gain that was initiated on the same DOWN event.
                        focusManager.clearFocus()
                    }
                )
            }
    ) {
        content()
    }
}