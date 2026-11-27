package com.saurabh.mediadminapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveContent
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveWidths
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.DetailCard
import com.saurabh.mediadminapp.ui.components.GsTextField
import com.saurabh.mediadminapp.ui.components.LinkButton
import com.saurabh.mediadminapp.ui.components.ShadeButton
import kotlinx.coroutines.delay

private const val CODE_LENGTH = 6
private const val RESEND_COOLDOWN_S = 60

/**
 * Change the signed-in admin's password, confirmed by a code emailed to the account.
 * The server signs out every session after the change, so the admin signs in again.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(viewModel: MyViewModel, navController: NavController) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val adminState by viewModel.getAllAdminState.collectAsStateWithLifecycle()
    val loggedInAdminId by viewModel.loggedInAdminId.collectAsStateWithLifecycle()
    val email = adminState.success?.items?.find { it.adminId == loggedInAdminId }?.email.orEmpty()
    val sendState by viewModel.passwordResetState.collectAsStateWithLifecycle()
    val saveState by viewModel.passwordResetOtpState.collectAsStateWithLifecycle()

    var codeSent by rememberSaveable { mutableStateOf(false) }
    var otp by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var otpError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmError by rememberSaveable { mutableStateOf<String?>(null) }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }
    var cooldown by rememberSaveable { mutableIntStateOf(0) }
    var started by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!started) {
            started = true
            viewModel.clearPasswordResetStates() // drop leftovers from the signed-out "Forgot password" flow
        }
        viewModel.getAllAdmin()
    }

    LaunchedEffect(cooldown) {
        if (cooldown > 0) {
            delay(1000L)
            cooldown--
        }
    }

    LaunchedEffect(sendState) {
        when {
            sendState.success != null -> {
                codeSent = true
                cooldown = RESEND_COOLDOWN_S
                viewModel.clearPasswordResetStates()
            }
            sendState.error != null -> {
                formError = sendState.error
                viewModel.clearPasswordResetStates()
            }
        }
    }

    LaunchedEffect(saveState) {
        when {
            saveState.success != null -> {
                viewModel.clearPasswordResetStates()
                Toast.makeText(context, "Password changed. Sign in with your new password.", Toast.LENGTH_LONG).show()
                viewModel.setAdminLoggedOut()
            }
            saveState.error != null -> {
                otpError = saveState.appError.fieldError("otp")
                passwordError = saveState.appError.fieldError("new_password")
                formError = if (otpError == null && passwordError == null) saveState.error else null
                viewModel.clearPasswordResetStates()
            }
        }
    }

    fun sendCode() {
        if (email.isBlank() || sendState.isLoading || cooldown > 0) return
        formError = null
        otpError = null
        viewModel.requestPasswordReset(email)
    }

    fun save() {
        if (saveState.isLoading) return
        otpError = if (otp.length != CODE_LENGTH) "Enter the $CODE_LENGTH-digit code" else null
        passwordError = if (password.length !in 8..128) "8-128 characters" else null
        confirmError = if (password != confirm) "Passwords do not match" else null
        if (otpError != null || passwordError != null || confirmError != null) return
        focusManager.clearFocus()
        formError = null
        viewModel.resetPasswordOtp(email, otp, password)
    }

    AppScreenScaffold(title = "Change password", onBack = { navController.popBackStack() }) { padding ->
        AdaptiveContent(Modifier.padding(padding), maxWidth = AdaptiveWidths.Form) {
            Column(
                Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DetailCard(title = "Email verification", icon = Icons.Outlined.MarkEmailRead) {
                    Text(
                        when {
                            email.isBlank() -> "Loading your account..."
                            codeSent -> "A code was sent to $email."
                            else -> "We will send a verification code to $email."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (codeSent) {
                        LinkButton(
                            if (cooldown > 0) "Send a new code in ${cooldown}s" else "Send a new code",
                            ::sendCode, enabled = cooldown == 0 && !sendState.isLoading,
                        )
                    }
                }
                if (codeSent) {
                    DetailCard(title = "New password", icon = Icons.Outlined.Lock) {
                        GsTextField(
                            value = otp,
                            onValueChange = { otp = it.filter(Char::isDigit).take(CODE_LENGTH); otpError = null },
                            label = { Text("Verification code") }, singleLine = true,
                            isError = otpError != null, supportingText = otpError?.let { { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        PasswordField(
                            value = password, onValueChange = { password = it; passwordError = null },
                            label = "New password", error = passwordError, hint = "8-128 characters",
                            imeAction = ImeAction.Next,
                        )
                        PasswordField(
                            value = confirm, onValueChange = { confirm = it; confirmError = null },
                            label = "Confirm password", error = confirmError,
                            imeAction = ImeAction.Done, onDone = ::save,
                        )
                    }
                }
                AuthFormError(formError)
                if (codeSent) {
                    ShadeButton("Change password", ::save, Modifier.fillMaxWidth(), isLoading = saveState.isLoading)
                } else {
                    ShadeButton(
                        "Send verification code", ::sendCode, Modifier.fillMaxWidth(),
                        enabled = email.isNotBlank(), isLoading = sendState.isLoading,
                    )
                }
            }
        }
    }
}
