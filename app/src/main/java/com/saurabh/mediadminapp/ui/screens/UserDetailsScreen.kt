package com.saurabh.mediadminapp.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.UserItem
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayDangerButton
import com.saurabh.mediadminapp.ui.screens.components.ClayEmptyState
import com.saurabh.mediadminapp.ui.screens.components.ClayErrorScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayGradientBackdrop
import com.saurabh.mediadminapp.ui.screens.components.ClayLoadingScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayPrimaryButton
import com.saurabh.mediadminapp.ui.screens.components.ClayStatusBadge
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.ui.theme.ClayAccent
import com.saurabh.mediadminapp.ui.theme.ClayBorder
import com.saurabh.mediadminapp.ui.theme.ClayFieldBg
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClaySecondary
import com.saurabh.mediadminapp.ui.theme.ClayStateApproved
import com.saurabh.mediadminapp.ui.theme.ClayStateBlocked
import com.saurabh.mediadminapp.ui.theme.ClayStatePending
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary

@Composable
fun UserDetailsScreen(user_id: String, viewModel: MyViewModel, navController: NavController) {
    val usersState = viewModel.getAllUserState.collectAsState()
    val deleteState = viewModel.deleteUserState.collectAsState().value
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.getAllUsers()
    }

    LaunchedEffect(key1 = deleteState.success) {
        if (deleteState.success != null && !deleteState.isLoading && deleteState.error == null) {
            Toast.makeText(context, deleteState.success.message, Toast.LENGTH_LONG).show()
            viewModel.resetDeleteUserState()
            navController.navigate(Routes.HomeRoutes()) {
                popUpTo(Routes.HomeRoutes()) { inclusive = true }
            }
        }
    }

    LaunchedEffect(key1 = deleteState.error) {
        if (deleteState.error != null && !deleteState.isLoading) {
            Toast.makeText(context, deleteState.error, Toast.LENGTH_LONG).show()
            viewModel.resetDeleteUserState()
        }
    }

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        ClayGradientBackdrop {
            when {
                usersState.value.isLoading -> {
                    ClayLoadingScreen(modifier = Modifier.padding(innerPadding))
                }
                usersState.value.error != null -> {
                    ClayErrorScreen(
                        errorMessage = usersState.value.error.toString(),
                        modifier = Modifier.padding(innerPadding),
                        onRetry = { viewModel.getAllUsers() }
                    )
                }
                usersState.value.success != null -> {
                    val users = usersState.value.success?.users
                    val user = users?.find { it.user_id == user_id }

                    if (user != null) {
                        UserDetailsHub(
                            user = user,
                            isDeleting = deleteState.isLoading,
                            onDeleteClick = { viewModel.deleteUser(user.user_id) },
                            onUpdateClick = { navController.navigate(Routes.UpdateUserDetailsRoutes.invoke(user.user_id)) },
                            navController = navController,
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        ClayEmptyState(
                            message = "User not found or has been deleted",
                            emoji = "👤",
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Detail Screen Architecture for User Profile:
 * Avatar-Centric Identity Hub, Clearance Matrix, Full-Width Specifications,
 * Recessed Inset Clusters, and Primary Admin Controls.
 * Zero Modifier.shadow() — all hardware canvas BlurMaskFilter shadows.
 */
@Composable
fun UserDetailsHub(
    user: UserItem,
    isDeleting: Boolean,
    onDeleteClick: () -> Unit,
    onUpdateClick: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val initial = user.name.firstOrNull()?.toString()?.uppercase() ?: "U"
    val isApproved = user.isApproved
    val isBlocked = user.block

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp)
    ) {
        // ── 1. TOP APP BAR ───────────────────────────────────────────────
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
                    text = "User Dossier",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // ── 2. AVATAR-CENTRIC IDENTITY HUB ───────────────────────────────
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hardware BlurMaskFilter Shadow Avatar (Anti-aliased)
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
                    Text(
                        text = initial,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Black,
                        color = ClayPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = user.name,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Role Clearance Badges Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClayStatusBadge(
                        text = if (isApproved) "Verified Member" else "Pending Verification",
                        color = if (isApproved) ClayStateApproved else ClayStatePending
                    )

                    if (isBlocked) {
                        ClayStatusBadge(
                            text = "Account Blocked",
                            color = ClayStateBlocked
                        )
                    }
                }
            }
        }

        // ── 3. ROLE CLEARANCE MATRIX & IDENTITY CARD ─────────────────────
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
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ClayPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = ClayPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Clearance & Access Matrix",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClayTextPrimary
                        )
                    }

                    // 2-Column Matrix Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ClearanceTile(
                            modifier = Modifier.weight(1f),
                            label = "ACCESS CLEARANCE",
                            value = if (isApproved) "Approved" else "Pending",
                            tint = if (isApproved) ClayStateApproved else ClayStatePending,
                            icon = Icons.Default.CheckCircle
                        )
                        ClearanceTile(
                            modifier = Modifier.weight(1f),
                            label = "SYSTEM STATUS",
                            value = if (isBlocked) "Blocked" else "Active",
                            tint = if (isBlocked) ClayStateBlocked else ClayStateApproved,
                            icon = if (isBlocked) Icons.Default.Block else Icons.Default.CheckCircle
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ClearanceTile(
                            modifier = Modifier.weight(1f),
                            label = "CLIENT ID",
                            value = user.user_id,
                            tint = ClayPrimary,
                            icon = Icons.Default.Badge
                        )
                        ClearanceTile(
                            modifier = Modifier.weight(1f),
                            label = "REGISTERED",
                            value = user.date_of_account_creation ?: "Active",
                            tint = ClaySecondary,
                            icon = Icons.Default.CalendarMonth
                        )
                    }
                }
            }
        }

        // ── 4. FULL-WIDTH CONTACT & ADDRESS SPECIFICATIONS ────────────────
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
                        text = "Contact & Shipping Dossier",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary
                    )

                    RecessedSpecRow(
                        icon = Icons.Default.Phone,
                        label = "Telephone Number",
                        value = user.phone_number.ifEmpty { "Not Provided" }
                    )
                    RecessedSpecRow(
                        icon = Icons.Default.Email,
                        label = "Email Address",
                        value = user.email.ifEmpty { "Not Provided" }
                    )
                    RecessedSpecRow(
                        icon = Icons.Default.Home,
                        label = "Street Address",
                        value = user.address.ifEmpty { "Not Provided" }
                    )
                    RecessedSpecRow(
                        icon = Icons.Default.LocationOn,
                        label = "Postal Pincode",
                        value = user.pin_code.ifEmpty { "Not Provided" }
                    )
                }
            }
        }

        // ── 5. PRIMARY ADMIN CONTROLS ────────────────────────────────────
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ClayPrimaryButton(
                    text = "Edit Profile & Permissions",
                    icon = Icons.Default.Edit,
                    onClick = onUpdateClick,
                    enabled = !isDeleting
                )

                ClayDangerButton(
                    text = if (isDeleting) "Processing Deletion..." else "Suspend / Delete User",
                    icon = Icons.Default.DeleteForever,
                    onClick = onDeleteClick,
                    enabled = !isDeleting,
                    isLoading = isDeleting
                )
            }
        }
    }
}

@Composable
private fun ClearanceTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    tint: Color,
    icon: ImageVector
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(ClayFieldBg)
            .border(1.dp, Color.White, RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ClayTextSecondary,
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ClayTextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RecessedSpecRow(
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
            Icon(icon, contentDescription = null, tint = ClayPrimary, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 11.sp, color = ClayTextSecondary, fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 14.sp, color = ClayTextPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun UserDetailsScreen1(user_id: String, viewModel: MyViewModel, navController: NavController) {
    UserDetailsScreen(user_id = user_id, viewModel = viewModel, navController = navController)
}