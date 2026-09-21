package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AdminPanelSettings
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
import com.saurabh.mediadminapp.ui.screens.components.ClayDangerButton
import com.saurabh.mediadminapp.ui.screens.components.ClayGradientBackdrop
import com.saurabh.mediadminapp.ui.screens.components.ClayInfoRow
import com.saurabh.mediadminapp.ui.theme.ClayBorder
import com.saurabh.mediadminapp.ui.theme.ClayCardBg
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClaySecondary
import com.saurabh.mediadminapp.ui.theme.ClaySuccess
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary

@Composable
fun ProfileScreen(viewModel: MyViewModel, navController: NavController) {
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.getAllAdmin()
    }

    val adminState by viewModel.getAllAdminState.collectAsState()
    val loggedInAdminId by viewModel.loggedInAdminId.collectAsState()
    val admin = adminState.success?.admins?.find { it.admin_id == loggedInAdminId }
    val initial = admin?.name?.firstOrNull()?.toString()?.uppercase() ?: "A"

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        ClayGradientBackdrop {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── AppBar on gradient ────────────────────────────────────────
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
                        text = "Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Avatar Circle ─────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(
                            elevation = 24.dp,
                            shape = CircleShape,
                            ambientColor = ClayPrimary.copy(alpha = 0.35f),
                            spotColor = ClayPrimary.copy(alpha = 0.35f)
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.6f))
                            )
                        )
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ClayPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name + role on gradient
                Text(
                    text = admin?.name ?: "Loading...",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "  ${admin?.role?.uppercase() ?: "ADMIN"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // ── Details Card ──────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = ClayPrimary.copy(0.18f),
                            spotColor = ClayPrimary.copy(0.22f)
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(ClayCardBg)
                        .border(1.5.dp, Color.White, RoundedCornerShape(28.dp))
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Account Information",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ClayInfoRow(label = "Email", value = admin?.email ?: "...")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = ClayBorder.copy(0.4f))
                    ClayInfoRow(label = "Phone", value = admin?.phone_number ?: "...")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = ClayBorder.copy(0.4f))
                    ClayInfoRow(label = "Account Created", value = admin?.date_of_account_creation ?: "...")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = ClayBorder.copy(0.4f))
                    ClayInfoRow(label = "Admin ID", value = admin?.admin_id ?: "...")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Logout button ─────────────────────────────────────────────
                ClayDangerButton(
                    text = "Logout",
                    onClick = { viewModel.setAdminLoggedOut() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}