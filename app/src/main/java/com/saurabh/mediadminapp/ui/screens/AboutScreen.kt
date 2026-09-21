package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayGradientBackdrop
import com.saurabh.mediadminapp.ui.screens.components.ClayStatCard
import com.saurabh.mediadminapp.ui.theme.ClayAccent
import com.saurabh.mediadminapp.ui.theme.ClayBadgeApproved
import com.saurabh.mediadminapp.ui.theme.ClayFieldBg
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClaySecondary
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary

@Composable
fun AboutScreen(viewModel: MyViewModel, navController: NavController) {
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp)
            ) {
                // ── 1. TOP APP BAR ───────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
                            text = "Platform Architecture",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                // ── 2. HERO MEDICAL EMBLEM & IDENTITY HUB ────────────────
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Hardware Canvas Blurred Shadow Icon
                        Box(
                            modifier = Modifier
                                .size(116.dp)
                                .drawBehind {
                                    val r = size.width / 2f
                                    drawIntoCanvas { canvas ->
                                        canvas.nativeCanvas.drawCircle(
                                            size.width / 2f,
                                            size.height / 2f + 8.dp.toPx(),
                                            r,
                                            android.graphics.Paint().apply {
                                                isAntiAlias = true
                                                color = android.graphics.Color.argb(75, 108, 99, 255)
                                                maskFilter = android.graphics.BlurMaskFilter(
                                                    22.dp.toPx(),
                                                    android.graphics.BlurMaskFilter.Blur.NORMAL
                                                )
                                            }
                                        )
                                    }
                                }
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.White, Color(0xFFFAF9FF), Color(0xFFEDE9FE))
                                    )
                                )
                                .border(2.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = "App Emblem",
                                tint = ClayPrimary,
                                modifier = Modifier.size(54.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "MediAdmin Enterprise",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Release Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(50.dp))
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "VERSION 1.0.0  •  PRODUCTION BUILD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // ── 3. LIVE SYSTEM TELEMETRY BENTO GRID ───────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ClayStatCard(
                            modifier = Modifier.weight(1f),
                            value = totalUsers.toString(),
                            label = "Active Users",
                            accentColor = ClayBadgeApproved,
                            icon = Icons.Default.People
                        )

                        ClayStatCard(
                            modifier = Modifier.weight(1f),
                            value = totalProducts.toString(),
                            label = "Catalog Stock",
                            accentColor = ClaySecondary,
                            icon = Icons.Default.ShoppingBag
                        )

                        ClayStatCard(
                            modifier = Modifier.weight(1f),
                            value = "Online",
                            label = "Cloud Hub",
                            accentColor = ClayPrimary,
                            icon = Icons.Default.Hub
                        )
                    }
                }

                // ── 4. ARCHITECTURE & SPECIFICATION DECK ─────────────────
                item {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 28.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(ClayPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DataObject,
                                        contentDescription = null,
                                        tint = ClayPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "System Architecture & Stack",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ClayTextPrimary
                                )
                            }

                            AboutSpecRow(
                                label = "UI Architecture",
                                value = "100% Jetpack Compose with Multi-pass Claymorphic Canvas Shading"
                            )
                            AboutSpecRow(
                                label = "State Management",
                                value = "Kotlin Coroutines, StateFlow, ViewModel & Hilt DI"
                            )
                            AboutSpecRow(
                                label = "Networking & IO",
                                value = "Retrofit2, OkHttp, Multipart Binary Image Pipelines"
                            )
                            AboutSpecRow(
                                label = "Backend Engine",
                                value = "Flask REST API + SQLite Relational Data Store"
                            )
                        }
                    }
                }

                // ── 5. ENGINEERING ATTRIBUTION BENTO CELL ────────────────
                item {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 28.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(ClayAccent.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = null,
                                        tint = ClayAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Craftsmanship & Design",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ClayTextPrimary
                                )
                            }

                            Text(
                                text = "Crafted by Saurabh. Architected using uncompromising tactile Claymorphism, custom hardware-canvas BlurMaskFilter lighting shaders, and an anti-cookie-cutter modular blueprint per screen.",
                                fontSize = 13.sp,
                                color = ClayTextSecondary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutSpecRow(label: String, value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ClayFieldBg)
            .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ClayTextSecondary
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = ClayTextPrimary
            )
        }
    }
}
