package com.saurabh.mediadminapp.ui.auth.reset

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saurabh.mediadminapp.ui.auth.Validators
import com.saurabh.mediadminapp.ui.auth.components.CuraTopBar
import com.saurabh.mediadminapp.ui.auth.components.OtpInput
import com.saurabh.mediadminapp.ui.auth.components.PasswordTextField
import com.saurabh.mediadminapp.ui.auth.components.PrimaryButton
import com.saurabh.mediadminapp.ui.auth.theme.CuraAuthTheme
import com.saurabh.mediadminapp.ui.auth.theme.LocalCuraExtendedColors
import com.saurabh.mediadminapp.ui.auth.theme.Spacing

@Composable
fun ResetPasswordScreen(
    email: String,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: ResetPasswordViewModel = viewModel(factory = ResetPasswordViewModel.Factory(email)),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ResetEvent.NavigateToSuccess -> onSuccess()
                is ResetEvent.ShowSnackbar      -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { CuraTopBar(title = "Reset password", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        ResetContent(
            email = email,
            uiState = uiState,
            onCodeChange = viewModel::onCodeChange,
            onNewPasswordChange = viewModel::onNewPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onNewPasswordFocusLost = viewModel::onNewPasswordFocusLost,
            onConfirmFocusLost = viewModel::onConfirmFocusLost,
            onReset = viewModel::onReset,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun ResetContent(
    email: String,
    uiState: ResetUiState,
    onCodeChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onNewPasswordFocusLost: () -> Unit,
    onConfirmFocusLost: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val extended = LocalCuraExtendedColors.current
    val maskedEmail = remember(email) { Validators.maskEmail(email) }

    val canReset = uiState.code.length == 6 &&
            uiState.has8Chars && uiState.hasNumber && uiState.hasSymbol &&
            uiState.confirmPassword == uiState.newPassword && uiState.newPassword.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = Spacing.screenHorizontal),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = "Enter the code sent to $maskedEmail.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.xl))

            // OTP boxes
            OtpInput(
                code = uiState.code,
                onCodeChange = onCodeChange,
                onComplete = {},
                isError = uiState.codeError != null,
                enabled = !uiState.isLoading,
            )

            AnimatedVisibility(visible = uiState.codeError != null, enter = fadeIn(), exit = fadeOut()) {
                uiState.codeError?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // New password
            PasswordTextField(
                value = uiState.newPassword,
                onValueChange = onNewPasswordChange,
                label = "New password",
                placeholder = "Enter new password",
                isError = uiState.newPasswordError != null,
                errorMessage = uiState.newPasswordError,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.onFocusChanged { if (!it.isFocused) onNewPasswordFocusLost() }
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // Confirm password
            PasswordTextField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = "Confirm password",
                placeholder = "Re-enter your password",
                isError = uiState.confirmPasswordError != null,
                errorMessage = uiState.confirmPasswordError,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onReset() }),
                modifier = Modifier.onFocusChanged { if (!it.isFocused) onConfirmFocusLost() }
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Password strength checklist
            Text(
                text = "Use 8+ characters with a number and a symbol.",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.sm))

            PasswordRule(label = "At least 8 characters", met = uiState.has8Chars)
            PasswordRule(label = "Contains a number", met = uiState.hasNumber)
            PasswordRule(label = "Contains a symbol", met = uiState.hasSymbol)
        }

        // Bottom CTA
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(Spacing.xl))
            PrimaryButton(
                text = "Reset password",
                onClick = onReset,
                isLoading = uiState.isLoading,
                enabled = canReset,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

@Composable
private fun PasswordRule(label: String, met: Boolean) {
    val extended = LocalCuraExtendedColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (met) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = if (met) "Rule met: $label" else "Rule not met: $label",
            tint = if (met) extended.success else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                fontWeight = if (met) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (met) extended.success else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun ResetDefaultPreview() {
    CuraAuthTheme {
        ResetContent(
            email = "admin@curapharmacy.in",
            uiState = ResetUiState(code = "4819", newPassword = "Admin@123", has8Chars = true, hasNumber = true, hasSymbol = true),
            onCodeChange = {}, onNewPasswordChange = {}, onConfirmPasswordChange = {},
            onNewPasswordFocusLost = {}, onConfirmFocusLost = {}, onReset = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ResetDarkPreview() {
    CuraAuthTheme(darkTheme = true) {
        ResetContent(
            email = "admin@curapharmacy.in",
            uiState = ResetUiState(codeError = "That code is invalid or expired."),
            onCodeChange = {}, onNewPasswordChange = {}, onConfirmPasswordChange = {},
            onNewPasswordFocusLost = {}, onConfirmFocusLost = {}, onReset = {}
        )
    }
}
