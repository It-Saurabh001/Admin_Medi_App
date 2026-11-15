package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import android.util.Patterns
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.data.v1.error.ErrorCode
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.nav.Routes

private const val MIN_PASSWORD_LENGTH = 8
private const val MAX_PASSWORD_LENGTH = 128

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthDetailScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { innerPadding -> content(Modifier.padding(innerPadding)) }
}

@Composable
private fun FormError(text: String?) = AuthFormError(text)

/** Step 1: request a reset code for an admin email. */
@Composable
fun ForgotPasswordScreen(viewModel: MyViewModel, navController: NavHostController) {
    val state by viewModel.passwordResetState.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(state.success, state.error) {
        if (!submitted) return@LaunchedEffect
        if (state.success != null) {
            submitted = false
            navController.navigate(Routes.ResetPasswordRoutes.route) { launchSingleTop = true }
        } else if (state.error != null) {
            submitted = false
            emailError = state.appError.fieldError("email")
            formError = when {
                emailError != null -> null
                (state.appError as? AppError.Api)?.code == ErrorCode.RATE_LIMITED ->
                    "Too many requests. Please wait a moment and try again."
                else -> state.error
            }
        }
    }

    fun submit() {
        if (state.isLoading) return
        val e = email.trim()
        emailError = when {
            e.isEmpty() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(e).matches() -> "Enter a valid email"
            else -> null
        }
        if (emailError != null) return
        formError = null
        submitted = true
        viewModel.requestPasswordReset(e)
    }

    AuthDetailScaffold(title = "Forgot password", onBack = { navController.navigateUp() }) { modifier ->
        AuthFormColumn(modifier = modifier, verticalArrangement = Arrangement.Top) {
            AuthHeader(
                icon = Icons.Outlined.LockReset,
                title = "Reset your password",
                subtitle = "Enter your admin email. If it is registered, we will send a 6-digit reset code."
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = null; formError = null },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                singleLine = true,
                enabled = !state.isLoading,
                isError = emailError != null,
                supportingText = emailError?.let { { SupportingText(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            FormError(formError)
            Spacer(modifier = Modifier.height(16.dp))
            AuthPrimaryButton(text = "Send code", isLoading = state.isLoading, onClick = ::submit)
        }
    }
}

/** Step 2: code + new password; on success back to Login. */
@Composable
fun ResetPasswordScreen(viewModel: MyViewModel, navController: NavHostController) {
    val state by viewModel.passwordResetOtpState.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf(viewModel.lastResetEmail().orEmpty()) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var otp by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var otpError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmError by rememberSaveable { mutableStateOf<String?>(null) }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(state.success, state.error) {
        if (!submitted) return@LaunchedEffect
        if (state.success != null) {
            submitted = false
            runCatching {
                navController.getBackStackEntry(Routes.SignInRoutes.route)
                    .savedStateHandle[AUTH_MESSAGE_KEY] = "Password updated. Sign in with your new password."
            }
            if (!navController.popBackStack(Routes.SignInRoutes.route, inclusive = false)) {
                navController.navigate(Routes.SignInRoutes.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        } else if (state.error != null) {
            submitted = false
            emailError = state.appError.fieldError("email")
            otpError = state.appError.fieldError("otp")
            passwordError = state.appError.fieldError("new_password")
            formError = when {
                (state.appError as? AppError.Api)?.code == ErrorCode.RATE_LIMITED ->
                    "Too many incorrect attempts. Request a new code."
                emailError == null && otpError == null && passwordError == null -> state.error
                else -> null
            }
        }
    }

    fun submit() {
        if (state.isLoading) return
        val e = email.trim()
        emailError = when {
            e.isEmpty() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(e).matches() -> "Enter a valid email"
            else -> null
        }
        otpError = if (otp.length != 6) "Enter the 6-digit code" else null
        passwordError = when {
            password.length < MIN_PASSWORD_LENGTH -> "Minimum $MIN_PASSWORD_LENGTH characters"
            password.length > MAX_PASSWORD_LENGTH -> "Maximum $MAX_PASSWORD_LENGTH characters"
            else -> null
        }
        confirmError = if (confirm != password) "Passwords do not match" else null
        if (emailError != null || otpError != null || passwordError != null || confirmError != null) return
        formError = null
        submitted = true
        viewModel.resetPasswordOtp(email = e, otp = otp, newPassword = password)
    }

    AuthDetailScaffold(title = "Reset password", onBack = { navController.navigateUp() }) { modifier ->
        AuthFormColumn(modifier = modifier, verticalArrangement = Arrangement.Top) {
            AuthHeader(
                icon = Icons.Outlined.Password,
                title = "Choose a new password",
                subtitle = "Enter the code from your email and a new password."
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = null; formError = null },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                singleLine = true,
                enabled = !state.isLoading,
                isError = emailError != null,
                supportingText = emailError?.let { { SupportingText(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = otp,
                onValueChange = { input ->
                    val cleaned = input.filter { it.isDigit() }
                    if (cleaned.length <= 6) { otp = cleaned; otpError = null; formError = null }
                },
                label = { Text("Reset code") },
                leadingIcon = { Icon(Icons.Outlined.Pin, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
                singleLine = true,
                enabled = !state.isLoading,
                isError = otpError != null,
                supportingText = otpError?.let { { SupportingText(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordField(
                value = password,
                onValueChange = { password = it; passwordError = null; formError = null },
                label = "New password",
                error = passwordError,
                hint = "Minimum 8 characters",
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordField(
                value = confirm,
                onValueChange = { confirm = it; confirmError = null },
                label = "Confirm password",
                error = confirmError,
                onDone = ::submit
            )
            FormError(formError)
            Spacer(modifier = Modifier.height(16.dp))
            AuthPrimaryButton(text = "Reset password", isLoading = state.isLoading, onClick = ::submit)
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { navController.navigateUp() },
                enabled = !state.isLoading,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Request a new code", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
