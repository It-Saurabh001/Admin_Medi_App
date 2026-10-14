package com.saurabh.mediadminapp.ui.auth.login

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saurabh.mediadminapp.ui.auth.components.CuraTextField
import com.saurabh.mediadminapp.ui.auth.components.PasswordTextField
import com.saurabh.mediadminapp.ui.auth.components.PrimaryButton
import com.saurabh.mediadminapp.ui.auth.components.SecondaryTextButton
import com.saurabh.mediadminapp.ui.auth.theme.CuraAuthTheme
import com.saurabh.mediadminapp.ui.auth.theme.Spacing

@Composable
fun LoginScreen(
    onLoginSuccess: (email: String) -> Unit,
    onForgotClick: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.NavigateToOtp  -> onLoginSuccess(event.email)
                is LoginEvent.ShowSnackbar   -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LoginContent(
            uiState = uiState,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onEmailFocusLost = viewModel::onEmailFocusLost,
            onPasswordFocusLost = viewModel::onPasswordFocusLost,
            onForgotClick = onForgotClick,
            onSubmit = viewModel::onSubmit,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onEmailFocusLost: () -> Unit,
    onPasswordFocusLost: () -> Unit,
    onForgotClick: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding()
            .padding(horizontal = Spacing.screenHorizontal),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(56.dp))

            // Logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(18.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Cura logo",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = "Sign in to manage your pharmacy.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.xxl))

            // Email field
            CuraTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = "Email",
                placeholder = "admin@curapharmacy.in",
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.onFocusChanged { if (!it.isFocused) onEmailFocusLost() }
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Password field
            PasswordTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = "Password",
                placeholder = "Enter your password",
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                modifier = Modifier.onFocusChanged { if (!it.isFocused) onPasswordFocusLost() }
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Forgot password right-aligned
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                SecondaryTextButton(
                    text = "Forgot password?",
                    onClick = onForgotClick,
                    enabled = !uiState.isLoading
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Credentials error banner
            AnimatedVisibility(
                visible = uiState.credentialsError != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                uiState.credentialsError?.let { errMsg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(Spacing.md)
                    ) {
                        Text(
                            text = errMsg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.md))
            }
        }

        // Bottom-anchored login button
        Column(modifier = Modifier.fillMaxWidth()) {
            PrimaryButton(
                text = "Log in",
                onClick = onSubmit,
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun LoginDefaultPreview() {
    CuraAuthTheme {
        LoginContent(
            uiState = LoginUiState(),
            onEmailChange = {}, onPasswordChange = {},
            onEmailFocusLost = {}, onPasswordFocusLost = {},
            onForgotClick = {}, onSubmit = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginErrorPreview() {
    CuraAuthTheme {
        LoginContent(
            uiState = LoginUiState(
                email = "bad",
                emailError = "Enter a valid email address.",
                credentialsError = "Incorrect email or password. Try again.",
                emailTouched = true,
                passwordTouched = true,
            ),
            onEmailChange = {}, onPasswordChange = {},
            onEmailFocusLost = {}, onPasswordFocusLost = {},
            onForgotClick = {}, onSubmit = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginLoadingPreview() {
    CuraAuthTheme {
        LoginContent(
            uiState = LoginUiState(isLoading = true),
            onEmailChange = {}, onPasswordChange = {},
            onEmailFocusLost = {}, onPasswordFocusLost = {},
            onForgotClick = {}, onSubmit = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoginDarkPreview() {
    CuraAuthTheme(darkTheme = true) {
        LoginContent(
            uiState = LoginUiState(),
            onEmailChange = {}, onPasswordChange = {},
            onEmailFocusLost = {}, onPasswordFocusLost = {},
            onForgotClick = {}, onSubmit = {}
        )
    }
}
