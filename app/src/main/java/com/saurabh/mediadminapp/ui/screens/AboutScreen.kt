package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayInfoRow
import com.saurabh.mediadminapp.ui.theme.ClayBorder
import com.saurabh.mediadminapp.ui.theme.ClayHomeGradient
import com.saurabh.mediadminapp.ui.theme.ClayScreenBg
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(viewModel: MyViewModel, navController: NavController) {
    val scrollState = rememberScrollState()
    val adminId by viewModel.loggedInAdminId.collectAsState()
    val usersState by viewModel.getAllUserState.collectAsState()
    val productState by viewModel.getAllProduct.collectAsState()

    // Trigger initial data load if missing
    LaunchedEffect(Unit) {
        if (usersState.success == null) viewModel.getAllUsers()
        if (productState.success == null) viewModel.getAllProduct()
    }

    val totalUsers = usersState.success?.users?.size ?: 0
    val totalProducts = productState.success?.products?.size ?: 0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "About",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = ClayTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ClayTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ClayScreenBg
                )
            )
        },
        containerColor = ClayScreenBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Icon / Logo Box
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        brush = ClayHomeGradient,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "App Logo",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "MediAdmin",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ClayTextPrimary
            )

            Text(
                text = "Version 1.0.0 • Production Build",
                fontSize = 14.sp,
                color = ClayTextSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Live Admin & System Status Card
            ClayCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Active System Metrics",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ClayInfoRow(label = "Logged-In Admin", value = adminId ?: "Admin Session Active")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = ClayBorder.copy(0.5f))
                    ClayInfoRow(label = "Total Registered Users", value = "$totalUsers Users")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = ClayBorder.copy(0.5f))
                    ClayInfoRow(label = "Catalog Products", value = "$totalProducts Items")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Description Card
            ClayCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "App Description",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "MediAdmin is a modern, comprehensive management system for administering medical supply chains. Designed for admins to approve/manage users, update stock, register product additions, view sales analytics, and manage incoming orders in real-time.",
                        fontSize = 14.sp,
                        color = ClayTextSecondary,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Developer Card
            ClayCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Developer Information",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Developed with ❤️ by Saurabh.\nBuilt using Kotlin, Jetpack Compose, Hilt, Retrofit, and SQLite / Flask Backend.",
                        fontSize = 14.sp,
                        color = ClayTextSecondary,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
