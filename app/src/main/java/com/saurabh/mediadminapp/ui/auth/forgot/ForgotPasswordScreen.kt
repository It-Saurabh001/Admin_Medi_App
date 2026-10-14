package com.saurabh.mediadminapp.ui.auth.forgot

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saurabh.mediadminapp.ui.auth.components.CuraTextField
import com.saurabh.mediadminapp.ui.auth.components.CuraTopBar
import com.saurabh.mediadminapp.ui.auth.components.PrimaryButton
import com.saurabh.mediadminapp.ui.auth.components.SecondaryTextButton
import com.saurabh.mediadminapp.ui.auth.theme.CuraAuthTheme
import com.saurabh.mediadminapp.ui.auth.theme.Spacing

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onCodeSent: (email: String) -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ForgotEvent.NavigateToReset -> onCodeSent(event.email)
                is ForgotEvent.ShowSnackbar    -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { CuraTopBar(title = "Forgot password", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        ForgotContent(
            uiState = uiState,
            onEmailChange = viewModel::onEmailChange,
            onEmailFocusLost = viewModel::onEmailFocusLost,
            onSendCode = viewModel::onSendCode,
            onBack = onBack,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun ForgotContent(
    uiState: ForgotUiState,
    onEmailChange: (String) -> Unit,
    onEmailFocusLost: () -> Unit,
    onSendCode: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = Spacing.screenHorizontal),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = "Enter your admin email and we'll send a 6-digit reset code.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(Spacing.xxl))

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
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onSendCode()
                    }
                ),
                modifier = Modifier.onFocusChanged { if (!it.isFocused) onEmailFocusLost() }
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryButton(
                text = "Send code",
                onClick = onSendCode,
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            SecondaryTextButton(
                text = "Back to login",
                onClick = onBack,
                enabled = !uiState.isLoading
            )
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun ForgotDefaultPreview() {
    CuraAuthTheme {
        ForgotContent(
            uiState = ForgotUiState(),
            onEmailChange = {}, onEmailFocusLost = {}, onSendCode = {}, onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotErrorPreview() {
    CuraAuthTheme {
        ForgotContent(
            uiState = ForgotUiState(email = "unknown@mail.com", emailError = "This email isn't registered.", emailTouched = true),
            onEmailChange = {}, onEmailFocusLost = {}, onSendCode = {}, onBack = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ForgotDarkPreview() {
    CuraAuthTheme(darkTheme = true) {
        ForgotContent(
            uiState = ForgotUiState(isLoading = true),
            onEmailChange = {}, onEmailFocusLost = {}, onSendCode = {}, onBack = {}
        )
    }
}
