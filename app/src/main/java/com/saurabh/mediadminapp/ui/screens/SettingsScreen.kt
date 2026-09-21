package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayGradientBackdrop
import com.saurabh.mediadminapp.ui.theme.ClayBadgeApproved
import com.saurabh.mediadminapp.ui.theme.ClayFieldBg
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClaySecondary
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary

@Composable
fun SettingsScreen(viewModel: MyViewModel, navController: NavController) {
    var isDarkTheme by remember { mutableStateOf(false) }
    var receiveNotifications by remember { mutableStateOf(true) }
    var autoSyncData by remember { mutableStateOf(true) }
    val adminId by viewModel.loggedInAdminId.collectAsState()

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        ClayGradientBackdrop {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp)
            ) {
                // ── 1. APP BAR ───────────────────────────────────────────
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
                            text = "Admin Configuration",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                // ── 2. DIAGNOSTIC IDENTITY HUB (Hero Bento Tile) ─────────
                item {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 28.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(ClayBadgeApproved.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = ClayBadgeApproved,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "MediAdmin Session Radar",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ClayTextPrimary
                                    )
                                    Text(
                                        text = "Operator: ${adminId ?: "Authenticated Admin"}",
                                        fontSize = 12.sp,
                                        color = ClayTextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Live Status Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(ClayBadgeApproved.copy(alpha = 0.12f))
                                    .border(0.5.dp, ClayBadgeApproved.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ENCRYPTED",
                                    color = ClayBadgeApproved,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                // ── 3. CLUSTERED BENTO GRID: TACTILE SWITCH DECKS ────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Bento Cell 1: Dark / Light Mode Switch
                        BentoSwitchCard(
                            modifier = Modifier.weight(1f),
                            title = "Theme Mode",
                            subtitle = if (isDarkTheme) "Dark Canvas" else "Light Clay",
                            icon = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                            iconTint = ClayPrimary,
                            checked = isDarkTheme,
                            onCheckedChange = { isDarkTheme = it }
                        )

                        // Bento Cell 2: Push Notifications Switch
                        BentoSwitchCard(
                            modifier = Modifier.weight(1f),
                            title = "Notifications",
                            subtitle = if (receiveNotifications) "Active Alerts" else "Muted Stream",
                            icon = Icons.Default.NotificationsActive,
                            iconTint = ClaySecondary,
                            checked = receiveNotifications,
                            onCheckedChange = { receiveNotifications = it }
                        )
                    }
                }

                item {
                    // Bento Cell 3: Data Auto-Sync
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 24.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ClayFieldBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cached,
                                        contentDescription = null,
                                        tint = ClayPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Real-Time Catalog Sync",
                                        fontWeight = FontWeight.Bold,
                                        color = ClayTextPrimary,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Keep stock levels updated with database streams",
                                        fontSize = 12.sp,
                                        color = ClayTextSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = autoSyncData,
                                onCheckedChange = { autoSyncData = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ClayBadgeApproved,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFD0C8FF)
                                )
                            )
                        }
                    }
                }

                // ── 4. SECURITY & CREDENTIALS BENTO CLUSTER ──────────────
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
                                        .background(ClayPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VpnKey,
                                        contentDescription = null,
                                        tint = ClayPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Security & Session Keys",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ClayTextPrimary
                                )
                            }

                            SettingsNavigationRow(
                                icon = Icons.Default.Key,
                                title = "Change Master Password",
                                subtitle = "Update credentials and security keys",
                                onClick = {}
                            )

                            SettingsNavigationRow(
                                icon = Icons.Default.Devices,
                                title = "Device Registry & Active Terminals",
                                subtitle = "1 active Android session authorized",
                                onClick = {}
                            )

                            SettingsNavigationRow(
                                icon = Icons.Default.Tune,
                                title = "API Gateway Parameters",
                                subtitle = "Flask / SQLite Backend connection endpoint",
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BentoSwitchCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ClayCard(
        modifier = modifier,
        cornerRadius = 22.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = ClayBadgeApproved,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFD0C8FF)
                    )
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ClayTextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = ClayTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ClayFieldBg)
            .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
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
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = ClayTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = ClayTextSecondary
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = ClayTextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}
