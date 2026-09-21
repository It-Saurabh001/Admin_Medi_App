package com.saurabh.mediadminapp.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayGradientBackdrop
import com.saurabh.mediadminapp.ui.screens.components.ClayInfoRow
import com.saurabh.mediadminapp.ui.theme.ClayAccent
import com.saurabh.mediadminapp.ui.theme.ClayBorder
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClaySecondary
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary

@Composable
fun AboutScreen(viewModel: MyViewModel, navController: NavController) {
    val scrollState = rememberScrollState()
    val adminId by viewModel.loggedInAdminId.collectAsState()
    val usersState by viewModel.getAllUserState.collectAsState()
    val productState by viewModel.getAllProduct.collectAsState()

    LaunchedEffect(Unit) {
        if (usersState.success == null) viewModel.getAllUsers()
        if (productState.success == null) viewModel.getAllProduct()
    }

    val totalUsers = usersState.success?.users?.size ?: 0
    val totalProducts = productState.success?.products?.size ?: 0

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        ClayGradientBackdrop {
            // ── Scrollable Content ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Top AppBar row on gradient ────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "About",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Hero Logo Circle ──────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(
                            elevation = 24.dp,
                            shape = CircleShape,
                            ambientColor = ClayPrimary.copy(alpha = 0.3f),
                            spotColor = ClayPrimary.copy(alpha = 0.3f)
                        )
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = "App Logo",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // App name + version on gradient
                Text(
                    text = "MediAdmin",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Version 1.0.0  •  Production Build",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.82f),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(36.dp))

                // ── Cards Panel (sits on gradient, below hero) ────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Live Metrics Card
                    ClayCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(ClayPrimary, ClaySecondary)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.People,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "  Active System Metrics",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ClayTextPrimary
                            )
                        }

                        ClayInfoRow(label = "Logged-In Admin", value = adminId ?: "Admin Session Active")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = ClayBorder.copy(0.4f))
                        ClayInfoRow(label = "Total Registered Users", value = "$totalUsers Users")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = ClayBorder.copy(0.4f))
                        ClayInfoRow(label = "Catalog Products", value = "$totalProducts Items")
                    }

                    // App Description Card
                    ClayCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(ClaySecondary, ClayPrimary)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "  App Description",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ClayTextPrimary
                            )
                        }
                        Text(
                            text = "MediAdmin is a modern, comprehensive management system for administering medical supply chains. Designed for admins to approve/manage users, update stock, register product additions, view sales analytics, and manage incoming orders in real-time.",
                            fontSize = 14.sp,
                            color = ClayTextSecondary,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Start
                        )
                    }

                    // Developer Card
                    ClayCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(ClayAccent, ClayPrimary)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Code,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "  Developer Information",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ClayTextPrimary
                            )
                        }
                        Text(
                            text = "Developed with ❤️ by Saurabh.\nBuilt using Kotlin, Jetpack Compose, Hilt, Retrofit, and SQLite / Flask Backend.",
                            fontSize = 14.sp,
                            color = ClayTextSecondary,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
