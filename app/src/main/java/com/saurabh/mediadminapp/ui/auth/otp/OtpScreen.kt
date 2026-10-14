package com.saurabh.mediadminapp.ui.auth.otp

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saurabh.mediadminapp.ui.auth.Validators
import com.saurabh.mediadminapp.ui.auth.components.CuraTopBar
import com.saurabh.mediadminapp.ui.auth.components.OtpInput
import com.saurabh.mediadminapp.ui.auth.components.PrimaryButton
import com.saurabh.mediadminapp.ui.auth.components.SecondaryTextButton
import com.saurabh.mediadminapp.ui.auth.theme.CuraAuthTheme
import com.saurabh.mediadminapp.ui.auth.theme.LocalCuraExtendedColors
import com.saurabh.mediadminapp.ui.auth.theme.Spacing
import java.util.Locale

@Composable
fun OtpScreen(
    email: String,
    onVerified: () -> Unit,
    onBack: () -> Unit,
    viewModel: OtpViewModel = viewModel(factory = OtpViewModel.Factory(email)),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is OtpEvent.NavigateToHome  -> onVerified()
                is OtpEvent.ShowSnackbar    -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { CuraTopBar(title = "Verify it's you", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        OtpContent(
            email = email,
            uiState = uiState,
            onCodeChange = viewModel::onCodeChange,
            onVerify = viewModel::onVerify,
            onResend = viewModel::onResend,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun OtpContent(
    email: String,
    uiState: OtpUiState,
    onCodeChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = LocalCuraExtendedColors.current
    val maskedEmail = remember(email) { Validators.maskEmail(email) }

    val mm = uiState.secondsLeft / 60
    val ss = uiState.secondsLeft % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", mm, ss)

    val canVerify = uiState.code.length == 6 && uiState.secondsLeft > 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = Spacing.screenHorizontal),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = "We sent a 6-digit code to $maskedEmail.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.xxl))

            OtpInput(
                code = uiState.code,
                onCodeChange = onCodeChange,
                onComplete = { if (uiState.secondsLeft > 0) onVerify() },
                isError = uiState.errorMessage != null,
                enabled = !uiState.isVerifying && uiState.secondsLeft > 0,
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Error message
            AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = fadeIn(), exit = fadeOut()
            ) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Timer / expired message
            Text(
                text = when {
                    uiState.secondsLeft == 0 -> "This code has expired. Request a new one."
                    else -> "Code expires in $timeFormatted"
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = if (uiState.secondsLeft == 0) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Resend button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SecondaryTextButton(
                    text = if (uiState.canResend) "Resend code"
                    else "Resend code in $timeFormatted",
                    onClick = onResend,
                    enabled = uiState.canResend && !uiState.isVerifying && !uiState.isResending
                )
            }
        }

        // Bottom CTA
        Column(modifier = Modifier.fillMaxWidth()) {
            PrimaryButton(
                text = "Verify",
                onClick = onVerify,
                isLoading = uiState.isVerifying,
                enabled = canVerify,
            )
            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun OtpDefaultPreview() {
    CuraAuthTheme {
        OtpContent(
            email = "admin@curapharmacy.in",
            uiState = OtpUiState(code = "4819"),
            onCodeChange = {}, onVerify = {}, onResend = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OtpErrorPreview() {
    CuraAuthTheme {
        OtpContent(
            email = "admin@curapharmacy.in",
            uiState = OtpUiState(code = "000000", errorMessage = "That code isn't right. Check and try again."),
            onCodeChange = {}, onVerify = {}, onResend = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OtpDarkPreview() {
    CuraAuthTheme(darkTheme = true) {
        OtpContent(
            email = "admin@curapharmacy.in",
            uiState = OtpUiState(secondsLeft = 0, canResend = true),
            onCodeChange = {}, onVerify = {}, onResend = {}
        )
    }
}
