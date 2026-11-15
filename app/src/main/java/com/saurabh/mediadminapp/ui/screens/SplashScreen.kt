package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.R
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(navController: NavController, viewModel: MyViewModel) {
    // Boolean? -> true, false, or null (still checking)
    val isUserLoggedIn by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()
    val upgradeRequired by viewModel.upgradeRequiredVersion.collectAsStateWithLifecycle()
    val configError by viewModel.appConfigError.collectAsStateWithLifecycle()

    LaunchedEffect(isUserLoggedIn, upgradeRequired, configError) {
        // Wait for the token check; never jump to the wrong screen.
        val loggedIn = isUserLoggedIn ?: return@LaunchedEffect
        // Config failed with nothing cached: stay here and offer Retry (unless an update is forced).
        if (configError != null && upgradeRequired == null) return@LaunchedEffect
        Log.d("SplashScreen", "Token check completed: loggedIn=$loggedIn")
        delay(1200.milliseconds)

        val target = when {
            upgradeRequired != null -> Routes.UpgradeRequiredRoutes.route
            loggedIn -> Routes.HomeRoutes.route
            else -> Routes.SignInRoutes.route
        }
        navController.navigate(target) {
            popUpTo(Routes.SplashRoutes.route) { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Admin",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(24.dp))
            val error = configError
            if (error != null && upgradeRequired == null) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 360.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.getAppConfig() }) { Text("Retry") }
            } else {
                LinearProgressIndicator(
                    modifier = Modifier.width(120.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }
        }
    }
}
