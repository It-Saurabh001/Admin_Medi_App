package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.ui.theme.ClayScreenBg
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(navController: NavController, viewModel: MyViewModel) {
    // 1. Lifecycle-aware state collection (Boolean? -> true, false, or null)
    val isUserLoggedIn by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()

    LaunchedEffect(isUserLoggedIn) {
        // 2. Jab tak token check chal raha hai (null), wait karo — galat screen par jump mat karo!
        val loggedIn = isUserLoggedIn ?: return@LaunchedEffect

        Log.d("SplashScreen", "Token check completed: loggedIn=$loggedIn")
        delay(1200.milliseconds)

        // 3. NavHost ke exact route types ke saath match kiya gaya navigation:
        if (loggedIn) {
            Log.d("SplashScreen", "Navigating to HomeRoutes")
            // HomeRoutes String-based composable hai: composable(Routes.HomeRoutes.route)
            navController.navigate(Routes.HomeRoutes.route) {
                popUpTo(Routes.SplashRoutes.route) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            Log.d("SplashScreen", "Navigating to SignInRoutes")
            // SignInRoutes Type-Safe composable hai: composable<Routes.SignInRoutes>
            navController.navigate(Routes.SignInRoutes.route) {
                popUpTo(Routes.SplashRoutes.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        containerColor = ClayScreenBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(Color(0xFFE2E8F0), CircleShape)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = "Splash Logo",
                    modifier = Modifier.fillMaxSize(),
                    tint = Color(0xFF6366F1) // Indigo Primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "MediAdmin",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ClayTextPrimary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Managing Medical Logistics",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = ClayTextSecondary
            )
        }
    }
}