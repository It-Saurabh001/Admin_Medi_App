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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.theme.ClayScreenBg
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import com.saurabh.mediadminapp.ui.screens.nav.Routes

@Composable
fun SplashScreen(navController: NavController, viewModel: MyViewModel) {
    val isUserLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    Log.d("TAG", "SplashScreen: isLoggedIn = $isUserLoggedIn")

    LaunchedEffect(isUserLoggedIn) {
        Log.d("SplashScreen", "LaunchedEffect triggered with isUserLoggedIn=$isUserLoggedIn")
        delay(1500.milliseconds)
        if (isUserLoggedIn) {
            Log.d("SplashScreen", "Navigating to HomeRoutes")
            navController.navigate(Routes.HomeRoutes()) {
                popUpTo(Routes.SplashRoutes()) { inclusive = true }
            }
        } else {
            Log.d("SplashScreen", "Navigating to SignInRoutes")
            navController.navigate(Routes.SignInRoutes) {
                popUpTo(Routes.SplashRoutes()) { inclusive = true }
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