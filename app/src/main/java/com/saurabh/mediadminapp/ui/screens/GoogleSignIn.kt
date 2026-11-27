package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import com.saurabh.mediadminapp.R
import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.saurabh.mediadminapp.BuildConfig

sealed interface GoogleSignInResult {
    data class Success(val idToken: String, val email: String) : GoogleSignInResult
    data object Cancelled : GoogleSignInResult
    data class Failure(val message: String) : GoogleSignInResult
}

/** Credential Manager "Sign in with Google". The ID token is only handed to the backend, never stored or logged. */
object GoogleCredentials {
    private const val PLACEHOLDER = "YOUR_WEB_CLIENT_ID"

    /** [activityContext] must be an Activity context (the account picker is shown over it). */
    suspend fun requestIdToken(activityContext: Context): GoogleSignInResult {
        val clientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        if (clientId.isBlank() || clientId == PLACEHOLDER) {
            return GoogleSignInResult.Failure("Google sign-in is not configured")
        }
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(serverClientId = clientId).build())
            .build()
        return try {
            val credential = CredentialManager.create(activityContext).getCredential(activityContext, request).credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val google = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleSignInResult.Success(google.idToken, google.id)
            } else {
                GoogleSignInResult.Failure("Google sign-in failed. Please try again.")
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled
        } catch (e: NoCredentialException) {
            GoogleSignInResult.Failure("No Google account found on this device")
        } catch (e: GoogleIdTokenParsingException) {
            GoogleSignInResult.Failure("Google sign-in failed. Please try again.")
        } catch (e: GetCredentialException) {
            GoogleSignInResult.Failure("Google sign-in failed. Check your connection and try again.")
        }
    }

    /** Forget the selected Google account so the next sign-in shows the picker again. */
    suspend fun clear(context: Context) {
        try {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } catch (e: ClearCredentialException) {
            // Local credential state only; logout already succeeded.
        }
    }
}

@Composable
internal fun GoogleSignInButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    // Google's sign-in button: pill, fixed light / dark colours, gradient "G".
    val light = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val container = if (light) Color(0xFFFFFFFF) else Color(0xFF131314)
    val content = if (light) Color(0xFF1F1F1F) else Color(0xFFE3E3E3)
    val outline = if (light) Color(0xFF747775) else Color(0xFF8E918F)
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = CircleShape,
            border = BorderStroke(1.dp, outline),
            contentPadding = PaddingValues(horizontal = 16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = container,
                contentColor = content,
                disabledContainerColor = container,
                disabledContentColor = content.copy(alpha = 0.38f),
            ),
            modifier = Modifier.width(AUTH_BUTTON_WIDTH).height(AUTH_BUTTON_HEIGHT),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_google_logo),
                contentDescription = null,
                alpha = if (enabled) 1f else 0.38f,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Sign in with Google",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}
