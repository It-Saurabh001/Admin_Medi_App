package com.saurabh.mediadminapp.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.nav.Routes

@Composable
private fun StatusMessageScreen(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { innerPadding ->
        AuthFormColumn(modifier = Modifier.padding(innerPadding)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            AuthPrimaryButton(text = actionText, isLoading = false, onClick = onAction)
        }
    }
}

/** Blocking update screen (426 / min_app_version). Back is disabled. */
@Composable
fun UpgradeRequiredScreen(viewModel: MyViewModel) {
    val context = LocalContext.current
    val minVersion by viewModel.upgradeRequiredVersion.collectAsStateWithLifecycle()
    BackHandler(enabled = true) { /* blocked until the app is updated */ }

    val versionLine = minVersion?.takeIf { it != "latest" }?.let { " Minimum supported version: $it." } ?: ""
    StatusMessageScreen(
        icon = Icons.Outlined.SystemUpdate,
        iconTint = MaterialTheme.colorScheme.primary,
        title = "Update required",
        message = "This version of MediAdmin is no longer supported. Please update to continue.$versionLine",
        actionText = "Update",
        onAction = {
            val pkg = context.packageName
            val market = Intent(Intent.ACTION_VIEW, "market://details?id=$pkg".toUri())
            try {
                context.startActivity(market)
            } catch (_: ActivityNotFoundException) {
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$pkg".toUri())
                    )
                }.onFailure {
                    Toast.makeText(context, "No app store or browser found", Toast.LENGTH_LONG).show()
                }
            }
        }
    )
}

/** Shown when the server answers 403 ACCOUNT_BLOCKED. Logout returns to Login with a clean stack. */
@Composable
fun AccountBlockedScreen(viewModel: MyViewModel, navController: NavHostController) {
    fun logout() {
        viewModel.setAdminLoggedOut()
        navController.navigate(Routes.SignInRoutes.route) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }
    // Back behaves like Log out: never return into a blocked session.
    BackHandler(enabled = true) { logout() }
    StatusMessageScreen(
        icon = Icons.Outlined.Block,
        iconTint = MaterialTheme.colorScheme.error,
        title = "Account blocked",
        message = "Your admin account is blocked or disabled. Contact the pharmacy owner to restore access.",
        actionText = "Log out",
        onAction = ::logout
    )
}
