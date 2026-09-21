package com.saurabh.mediadminapp.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.UserItem
import com.saurabh.mediadminapp.ui.screens.components.ClayEmptyState
import com.saurabh.mediadminapp.ui.screens.components.ClayErrorScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayLoadingScreen
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.utils.ScreensState.IsApprovedUserState

// ── 1:1 Palette Mirrored from SignIn.kt ─────────────────────────────────────
private val ClayPrimary = Color(0xFF6C63FF)
private val ClaySecondary = Color(0xFF48CAE4)
private val ClayAccent = Color(0xFFFF6584)
private val ClayCardBg = Color(0xFFFAF9FF)
private val ClayFieldBg = Color(0xFFF0EEFF)
private val ClayBorder = Color(0xFFD0C8FF)
val ClayGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF6C63FF), Color(0xFF48CAE4))
)
val ClayCardShadow = Color(0xFF6C63FF).copy(alpha = 0.25f)
private val ClayApproved = Color(0xFF10B981)
private val ClayPending = Color(0xFFF59E0B)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(viewModel: MyViewModel, navController: NavController) {
    // 1. ट्रैकिंग: होम स्क्रीन कब कंपोज़ होना शुरू हुई
    Log.d("DRAWER_DEBUG", "🏠 HomeScreen: Composition Started")

    val state by viewModel.getAllUserState.collectAsState()
    val isApproved = viewModel.isApprovedUser.collectAsState()

    LaunchedEffect(Unit) {
        // 2. ट्रैकिंग: API कॉल कब फायर हुई
        Log.d("DRAWER_DEBUG", "🏠 HomeScreen: LaunchedEffect(Unit) Triggered -> Fetching users")
        viewModel.getAllUsers()
    }

    // 3. ट्रैकिंग: स्टेट कब-कब बदल रही है (Loading -> Success)
    LaunchedEffect(state.isLoading, state.success, state.error) {
        Log.d("DRAWER_DEBUG", "🏠 HomeScreen State Updated -> isLoading: ${state.isLoading}, isSuccess: ${state.success != null}, isError: ${state.error != null}")
    }

    // ── Full-screen vibrant SignIn background ──
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = ClayGradient)
    ) {
        // Decorative Blobs matching SignIn.kt
        ClayBlob1(
            modifier = Modifier
                .size(190.dp)
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 10.dp),
            color = Color.White.copy(alpha = 0.12f)
        )
        ClayBlob1(
            modifier = Modifier
                .size(130.dp)
                .align(Alignment.BottomStart)
                .padding(bottom = 50.dp, start = 10.dp),
            color = ClayAccent.copy(alpha = 0.22f)
        )

        when {
            state.isLoading -> {
                Log.d("DRAWER_DEBUG", "🏠 HomeScreen UI: Showing Loading Screen")
                ClayLoadingScreen(modifier = Modifier.fillMaxSize())
            }
            state.error != null -> {
                Log.d("DRAWER_DEBUG", "🏠 HomeScreen UI: Showing Error Screen")
                ClayErrorScreen(
                    errorMessage = state.error.toString(),
                    modifier = Modifier.fillMaxSize()
                )
            }
            state.success != null -> {
                Log.d("DRAWER_DEBUG", "🏠 HomeScreen UI: Showing UserListScreen1")
                UserListScreen1(
                    users = state.success!!.users,
                    userApprovalState = isApproved,
                    onApprovalToggle = viewModel::isApprovedUser,
                    modifier = Modifier.fillMaxSize(),
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun UserListScreen1(
    users: List<UserItem>,
    userApprovalState: State<Map<String, IsApprovedUserState>>,
    onApprovalToggle: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Log.d("DRAWER_DEBUG", "📜 UserListScreen1: Composition Started with ${users.size} users")
    var searchTerm by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf(FilterStatus.ALL) }

    val filteredUsers = remember(users, searchTerm, filterStatus) {
        Log.d("DRAWER_DEBUG", "📜 UserListScreen1: Filtering users (Search: '$searchTerm', Status: $filterStatus)")
        users.filter { user ->
            val matchesSearch = user.name.lowercase().contains(searchTerm.lowercase()) ||
                    user.email.lowercase().contains(searchTerm.lowercase()) ||
                    user.user_id.lowercase().contains(searchTerm.lowercase())

            val matchesFilter = when (filterStatus) {
                FilterStatus.ALL -> true
                FilterStatus.APPROVED -> user.isApproved && !user.block
                FilterStatus.PENDING -> !user.isApproved
                FilterStatus.BLOCKED -> user.block
            }
            matchesSearch && matchesFilter
        }
    }

    val stats = remember(users) {
        val total = users.size
        val approved = users.count { it.isApproved && !it.block }
        val pending = users.count { !it.isApproved }
        val blocked = users.count { it.block }
        val approvalRatio = if (total > 0) approved.toFloat() / total.toFloat() else 0f
        TotalUserStats(total, approved, pending, blocked, approvalRatio)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        // ── 1. Floating Pill / Dome Badge ──────────────────────────────────
//        item {
//            Box(
//                modifier = Modifier
//                    .size(80.dp)
//                    .shadow(
//                        elevation = 18.dp,
//                        shape = CircleShape,
//                        ambientColor = ClayCardShadow,
//                        spotColor = ClayCardShadow
//                    )
//                    .clip(CircleShape)
//                    .background(Color.White)
//                    .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "👥",
//                    fontSize = 36.sp
//                )
//            }
//        }
//
//        // ── 2. Screen Titles (Bold White like SignIn) ───────────────────────
//        item {
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                Text(
//                    text = "Admin Dashboard",
//                    fontSize = 28.sp,
//                    fontWeight = FontWeight.ExtraBold,
//                    color = Color.White
//                )
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = "Manage and verify platform users",
//                    fontSize = 14.sp,
//                    color = Color.White.copy(alpha = 0.85f)
//                )
//            }
//        }

        // ── 3. Chunky Hero Overview Card ────────────────────────────────────
        item {
            ClayHeroOverviewCard(stats = stats)
        }

        // ── 4. Stat Tiles Row ────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ClayStatTile(
                    label = "Approved",
                    value = stats.approved.toString(),
                    color = ClayApproved,
                    modifier = Modifier.weight(1f)
                )
                ClayStatTile(
                    label = "Pending",
                    value = stats.pending.toString(),
                    color = ClayPending,
                    modifier = Modifier.weight(1f)
                )
                ClayStatTile(
                    label = "Blocked",
                    value = stats.blocked.toString(),
                    color = ClayAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── 5. Search Field (Mirrored from SignIn ClayTextField) ────────────
        item {
            OutlinedTextField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                placeholder = { Text("Search users by name, id...", color = Color(0xFF888AAA), fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = ClayPrimary)
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClayPrimary,
                    unfocusedBorderColor = ClayBorder,
                    focusedContainerColor = ClayCardBg,
                    unfocusedContainerColor = ClayCardBg,
                    cursorColor = ClayPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(20.dp), ambientColor = ClayCardShadow, spotColor = ClayCardShadow)
            )
        }

        // ── 6. Filter Capsules ───────────────────────────────────────────────
        item {
            val filters = listOf(
                FilterItem(FilterStatus.ALL, "All Users", Icons.AutoMirrored.Filled.List),
                FilterItem(FilterStatus.APPROVED, "Approved", Icons.Default.CheckCircle),
                FilterItem(FilterStatus.PENDING, "Pending", Icons.Default.HourglassTop),
                FilterItem(FilterStatus.BLOCKED, "Blocked", Icons.Default.PersonOff)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(filters) { item ->
                    ClayFilterCapsule(
                        label = item.label,
                        icon = item.icon,
                        isSelected = filterStatus == item.status,
                        onClick = { filterStatus = item.status }
                    )
                }
            }
        }

        // ── 7. Each User Card List ───────────────────────────────────────────
        if (filteredUsers.isEmpty()) {
            item {
                ClayEmptyState(message = "No users found", emoji = "🔍")
            }
        } else {
            items(filteredUsers, key = { it.user_id }) { user ->
                EachUserCard(
                    userItem = user,
                    userApprovalState = userApprovalState,
                    onApprovalToggle = onApprovalToggle,
                    navController = navController
                )
            }
        }
    }
}

// ── Components ──────────────────────────────────────────────────────────────

@Composable
private fun ClayHeroOverviewCard(stats: TotalUserStats) {
    val animatedProgress by animateFloatAsState(
        targetValue = stats.ratio,
        animationSpec = tween(durationMillis = 900),
        label = "heroGauge"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = ClayCardShadow,
                spotColor = ClayCardShadow
            )
            .clip(RoundedCornerShape(28.dp))
            .background(ClayCardBg)
            .border(1.5.dp, Color.White, RoundedCornerShape(28.dp))
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(ClayFieldBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "OVERVIEW METRICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClayPrimary,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "${stats.total} Registered Users",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E1B4B)
                )
                Text(
                    text = "${(stats.ratio * 100).toInt()}% Verified Members",
                    fontSize = 13.sp,
                    color = Color(0xFF6B6893),
                    fontWeight = FontWeight.Medium
                )
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(76.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = ClayFieldBg,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(listOf(ClayPrimary, ClaySecondary, ClayPrimary)),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${stats.approved}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ClayPrimary
                )
            }
        }
    }
}

@Composable
private fun ClayStatTile(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = ClayCardShadow,
                spotColor = ClayCardShadow
            )
            .clip(RoundedCornerShape(22.dp))
            .background(ClayCardBg)
            .border(1.5.dp, Color.White, RoundedCornerShape(22.dp))
            .padding(vertical = 16.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E1B4B)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B6893)
            )
        }
    }
}

@Composable
private fun ClayFilterCapsule(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .shadow(
                elevation = if (isSelected) 10.dp else 4.dp,
                shape = shape,
                ambientColor = ClayCardShadow,
                spotColor = ClayCardShadow
            )
            .clip(shape)
            .background(
                if (isSelected) Brush.horizontalGradient(listOf(ClayPrimary, ClaySecondary))
                else Brush.linearGradient(listOf(Color.White, ClayCardBg))
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Transparent else Color.White,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else ClayPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) Color.White else Color(0xFF1E1B4B)
            )
        }
    }
}

@Composable
fun EachUserCard(
    userItem: UserItem,
    userApprovalState: State<Map<String, IsApprovedUserState>>,
    onApprovalToggle: (String, Boolean) -> Unit,
    navController: NavController
) {
    val currentUserState = userApprovalState.value[userItem.user_id]
    val isApproved = userItem.isApproved
    val isLoading = currentUserState?.isLoading == true

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = ClayCardShadow,
                spotColor = ClayCardShadow
            )
            .clip(RoundedCornerShape(28.dp))
            .background(ClayCardBg)
            .border(1.5.dp, Color.White, RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chunky Avatar Circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(8.dp, CircleShape, ambientColor = ClayCardShadow, spotColor = ClayCardShadow)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.5.dp, Color(0xFFE8E4FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userItem.name.firstOrNull()?.uppercase() ?: "U",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ClayPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userItem.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1B4B),
                        maxLines = 1
                    )
                    Text(
                        text = "ID: ${userItem.user_id.take(10)}...",
                        fontSize = 12.sp,
                        color = Color(0xFF888AAA)
                    )
                }

                val (badgeText, badgeColor) = when {
                    userItem.block -> "Blocked" to ClayAccent
                    isApproved -> "Approved" to ClayApproved
                    else -> "Pending" to ClayPending
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeColor.copy(alpha = 0.12f))
                        .border(1.dp, badgeColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recessed Well / Inset field
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(ClayFieldBg)
                    .border(1.dp, ClayBorder, RoundedCornerShape(18.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "📧 ${userItem.email ?: "No email"}", fontSize = 12.sp, color = Color(0xFF1E1B4B), fontWeight = FontWeight.Medium)
                    Text(text = "📞 ${userItem.phone_number ?: "N/A"}", fontSize = 12.sp, color = Color(0xFF888AAA))
                }
                userItem.address?.takeIf { it.isNotBlank() }?.let { addr ->
                    Text(text = "📍 $addr", fontSize = 12.sp, color = Color(0xFF666688), maxLines = 1)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = ClayPrimary.copy(alpha = 0.35f),
                            spotColor = ClayPrimary.copy(alpha = 0.35f)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(ClayPrimary, ClaySecondary)))
                        .clickable { navController.navigate(Routes.UserDetailsRoutes.invoke(userItem.user_id)) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "View Details →", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.width(16.dp))

                AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
                    CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 2.5.dp, color = ClayPrimary)
                }
                AnimatedVisibility(visible = !isLoading, enter = fadeIn(), exit = fadeOut()) {
                    Switch(
                        checked = isApproved,
                        onCheckedChange = { onApprovalToggle(userItem.user_id, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = ClayApproved,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFCBD5E1)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ClayBlob1(modifier: Modifier = Modifier, color: Color) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
    )
}

private data class TotalUserStats(
    val total: Int,
    val approved: Int,
    val pending: Int,
    val blocked: Int,
    val ratio: Float
)

private data class FilterItem(
    val status: FilterStatus,
    val label: String,
    val icon: ImageVector
)