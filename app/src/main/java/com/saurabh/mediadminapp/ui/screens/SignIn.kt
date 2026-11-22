package com.saurabh.mediadminapp.ui.screens

import com.saurabh.mediadminapp.ui.components.GsTextField
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.R
import com.saurabh.mediadminapp.data.v1.error.AppError
import com.saurabh.mediadminapp.data.v1.error.ErrorCode
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import kotlinx.coroutines.launch

/** Admin-only login: email + password; always account_type = admin (VM). No sign-up. */
@Composable
fun SignIn(viewModel: MyViewModel, navController: NavHostController) {
    val state by viewModel.loginAdminState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var formError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Message handed back from Otp/Reset (e.g. "Admin account required", "Password updated").
    val handle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(handle) {
        handle?.get<String>(AUTH_MESSAGE_KEY)?.let {
            formError = it
            handle.remove<String>(AUTH_MESSAGE_KEY)
        }
    }

    LaunchedEffect(state.error) {
        val error = state.error ?: return@LaunchedEffect
        val appError = state.appError
        when ((appError as? AppError.Api)?.code) {
            ErrorCode.ACCOUNT_BLOCKED ->
                navController.navigate(Routes.AccountBlockedRoutes.route) { launchSingleTop = true }
            ErrorCode.ACCOUNT_NOT_APPROVED ->
                formError = "This admin account is awaiting approval."
            ErrorCode.EMAIL_NOT_VERIFIED -> {
                viewModel.resendAdminEmailOtp(email.trim())
                navController.navigate(Routes.VerifyEmailRoutes(email.trim())) { launchSingleTop = true }
            }
            ErrorCode.RATE_LIMITED ->
                formError = "Too many attempts. Please wait a moment and try again."
            ErrorCode.ADMIN_ACCOUNT_NOT_REGISTERED ->
                formError = "No admin account uses this Google email. Create an account first."
            ErrorCode.GOOGLE_ACCOUNT_MISMATCH ->
                formError = "This account is linked to a different Google account. Use that one or your password."
            else -> {
                emailError = appError.fieldError("email")
                passwordError = appError.fieldError("password")
                formError = if (emailError == null && passwordError == null) error else null
            }
        }
        viewModel.clearLoginState()
    }

    LaunchedEffect(state.success) {
        val response = state.success ?: return@LaunchedEffect
        if (response.otpRequired) {
            navController.navigate(Routes.VerifyOtpRoutes(userId = response.accountId)) { launchSingleTop = true }
        } else {
            formError = "Invalid email or password"
        }
        viewModel.clearLoginState()
    }

    fun submit() {
        if (state.isLoading) return
        val e = email.trim()
        emailError = when {
            e.isEmpty() -> "Enter your email"
            !Patterns.EMAIL_ADDRESS.matcher(e).matches() -> "Enter a valid email"
            else -> null
        }
        passwordError = if (password.isEmpty()) "Enter your password" else null
        if (emailError != null || passwordError != null) return
        focusManager.clearFocus()
        formError = null
        viewModel.loginAdmin(e, password)
    }

    fun googleSignIn() {
        if (state.isLoading) return
        focusManager.clearFocus()
        formError = null
        scope.launch {
            when (val result = GoogleCredentials.requestIdToken(context)) {
                is GoogleSignInResult.Success -> viewModel.loginAdminWithGoogle(result.idToken)
                is GoogleSignInResult.Failure -> formError = result.message
                GoogleSignInResult.Cancelled -> Unit
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { innerPadding ->
        AuthFormColumn(modifier = Modifier.padding(innerPadding)) {
            Image(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sign in with your admin account",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            GsTextField(
                value = email,
                onValueChange = { email = it; emailError = null; formError = null },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, autoCorrectEnabled = false, imeAction = ImeAction.Next),
                singleLine = true,
                enabled = !state.isLoading,
                isError = emailError != null,
                supportingText = emailError?.let { { SupportingText(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            PasswordField(
                value = password,
                onValueChange = { password = it; passwordError = null; formError = null },
                label = "Password",
                error = passwordError,
                imeAction = ImeAction.Done,
                onDone = ::submit
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                com.saurabh.mediadminapp.ui.components.LinkButton(
                    text = "Forgot password?",
                    onClick = { navController.navigate(Routes.ForgotPasswordRoutes.route) { launchSingleTop = true } },
                    enabled = !state.isLoading,
                )
            }

            AuthFormError(formError)

            Spacer(modifier = Modifier.height(16.dp))
            AuthPrimaryButton(text = "Sign in", isLoading = state.isLoading, onClick = ::submit)
            Spacer(modifier = Modifier.height(12.dp))
            GoogleSignInButton(enabled = !state.isLoading, onClick = ::googleSignIn)
            // Shown unless app-config says admin signup is disabled.
            if (viewModel.adminSignupConfig()?.enabled != false) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(8.dp))
                AuthFooterLink(
                    prompt = "New admin?",
                    action = "Create account",
                    onClick = { navController.navigate(Routes.AdminRegisterRoutes.route) { launchSingleTop = true } }
                )
            }
        }
    }
}
