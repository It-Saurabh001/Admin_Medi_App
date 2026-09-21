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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
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
import com.saurabh.mediadminapp.ui.screens.components.ClayDangerButton
import com.saurabh.mediadminapp.ui.screens.components.ClayGradientBackdrop
import com.saurabh.mediadminapp.ui.screens.components.ClayStatusBadge
import com.saurabh.mediadminapp.ui.theme.ClayBadgeApproved
import com.saurabh.mediadminapp.ui.theme.ClayFieldBg
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClaySecondary
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary

@Composable
fun ProfileScreen(viewModel: MyViewModel, navController: NavController) {
    LaunchedEffect(Unit) {
        viewModel.getAllAdmin()
    }

    val adminState by viewModel.getAllAdminState.collectAsState()
    val loggedInAdminId by viewModel.loggedInAdminId.collectAsState()
    val admin = adminState.success?.admins?.find { it.admin_id == loggedInAdminId }
    val initial = admin?.name?.firstOrNull()?.toString()?.uppercase() ?: "A"

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
                            text = "Admin Identity",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                // ── 2. HERO AVATAR & CLEARANCE HUB ───────────────────────
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Hardware Canvas Blurred Shadow Avatar
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
                                                color = android.graphics.Color.argb(80, 108, 99, 255)
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
                                        listOf(Color.White, Color(0xFFFAF9FF), Color(0xFFE0E7FF))
                                    )
                                )
                                .border(2.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                color = ClayPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = admin?.name ?: "Administrator",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Role Clearance Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(50.dp))
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = admin?.role?.uppercase() ?: "ROOT OPERATOR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                // ── 3. DIAGNOSTIC RADAR BENTO CELL ───────────────────────
                item {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(ClayBadgeApproved.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = ClayBadgeApproved,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Terminal Authorization",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ClayTextPrimary
                                    )
                                    Text(
                                        text = "Active session validated & encrypted",
                                        fontSize = 12.sp,
                                        color = ClayTextSecondary
                                    )
                                }
                            }

                            ClayStatusBadge(
                                text = "ONLINE",
                                color = ClayBadgeApproved
                            )
                        }
                    }
                }

                // ── 4. CREDENTIALS & METADATA BENTO DECK ──────────────────
                item {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 28.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "System Dossier & Credentials",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ClayTextPrimary
                            )

                            ProfileSpecItem(
                                icon = Icons.Default.Email,
                                label = "Verified Email",
                                value = admin?.email ?: "admin@medisupply.com"
                            )
                            ProfileSpecItem(
                                icon = Icons.Default.Phone,
                                label = "Direct Telephone",
                                value = admin?.phone_number ?: "Active Line"
                            )
                            ProfileSpecItem(
                                icon = Icons.Default.Fingerprint,
                                label = "Admin Master ID",
                                value = admin?.admin_id ?: (loggedInAdminId ?: "ADM-001")
                            )
                            ProfileSpecItem(
                                icon = Icons.Default.CalendarMonth,
                                label = "Commission Date",
                                value = admin?.date_of_account_creation ?: "System Inception"
                            )
                        }
                    }
                }

                // ── 5. LOGOUT DANGER ACTION ──────────────────────────────
                item {
                    ClayDangerButton(
                        text = "Terminate Session & Logout",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        onClick = { viewModel.setAdminLoggedOut() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSpecItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ClayFieldBg)
            .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ClayPrimary,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = ClayTextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = ClayTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}