package com.saurabh.mediadminapp.ui.screens

import android.os.SystemClock
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.data.v1.error.ErrorCode
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import kotlinx.coroutines.delay

private const val EMAIL_OTP_LENGTH = 6
private const val RESEND_COOLDOWN_S = 60

/** Signup email verification: POST auth/verify-otp {email, otp}; resend via auth/resend-otp. */
@Composable
fun EmailVerifyScreen(email: String, viewModel: MyViewModel, navController: NavHostController) {
    val state by viewModel.emailVerifyState.collectAsStateWithLifecycle()
    var otp by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var info by rememberSaveable { mutableStateOf<String?>("We sent a 6-digit code to $email") }
    var resendAt by rememberSaveable { mutableLongStateOf(SystemClock.elapsedRealtime() + RESEND_COOLDOWN_S * 1000L) }
    var cooldown by remember { mutableIntStateOf(RESEND_COOLDOWN_S) }

    DisposableEffect(Unit) { onDispose { viewModel.clearEmailVerifyState() } }

    LaunchedEffect(resendAt) {
        while (true) {
            cooldown = ((resendAt - SystemClock.elapsedRealtime()) / 1000L).toInt().coerceAtLeast(0)
            if (cooldown == 0) break
            delay(1000L)
        }
    }

    LaunchedEffect(state) {
        when {
            state.verified -> {
                runCatching {
                    navController.getBackStackEntry(Routes.SignInRoutes.route)
                        .savedStateHandle[AUTH_MESSAGE_KEY] = "Email verified. Sign in with $email."
                }
                if (!navController.popBackStack(Routes.SignInRoutes.route, inclusive = false)) {
                    navController.navigate(Routes.SignInRoutes.route) { popUpTo(0) { inclusive = true } }
                }
            }
            state.resent -> {
                info = "A new code was sent to $email"
                error = null
                resendAt = SystemClock.elapsedRealtime() + RESEND_COOLDOWN_S * 1000L
                viewModel.clearEmailVerifyState()
            }
            state.error != null -> {
                val api = state.appError as? AppError.Api
                error = when (api?.code) {
                    ErrorCode.RATE_LIMITED -> state.error
                    ErrorCode.EMAIL_DELIVERY_FAILED -> "Could not send the email. Try again shortly."
                    else -> api?.fields?.get("otp")?.let { "${state.error}: $it" } ?: state.error
                }
                viewModel.clearEmailVerifyState()
            }
        }
    }

    fun verify() {
        if (otp.length != EMAIL_OTP_LENGTH) { error = "Enter all $EMAIL_OTP_LENGTH digits"; return }
        viewModel.verifyAdminEmail(email, otp)
    }

    Scaffold { inner ->
        AuthFormColumn(modifier = Modifier.padding(inner)) {
            AuthHeader(icon = Icons.Outlined.MarkEmailRead, title = "Verify your email", subtitle = info.orEmpty())
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = otp,
                onValueChange = { v -> otp = v.filter(Char::isDigit).take(EMAIL_OTP_LENGTH); error = null },
                label = { Text("Verification code") },
                singleLine = true,
                isError = error != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { verify() }),
                modifier = Modifier.fillMaxWidth()
            )
            AuthFormError(error)
            Spacer(Modifier.height(16.dp))
            AuthPrimaryButton(text = "Verify", isLoading = state.isLoading, onClick = { verify() })
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = { viewModel.resendAdminEmailOtp(email) },
                enabled = cooldown == 0 && !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (cooldown > 0) "Resend code in ${cooldown}s" else "Resend code")
            }
        }
    }
}
