package com.saurabh.mediadminapp.ui.screens

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
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp),
    ) {
        Text("Continue with Google", style = MaterialTheme.typography.labelLarge)
    }
}
