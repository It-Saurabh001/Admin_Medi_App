package com.saurabh.mediadminapp.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.UserItem
import com.saurabh.mediadminapp.ui.screens.components.ClayDetailButton
import com.saurabh.mediadminapp.ui.screens.components.ClayEmptyState
import com.saurabh.mediadminapp.ui.screens.components.ClayErrorScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayFilterChip
import com.saurabh.mediadminapp.ui.screens.components.ClayLoadingScreen
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.ui.theme.ClayAccent
import com.saurabh.mediadminapp.ui.theme.ClayBorder
import com.saurabh.mediadminapp.ui.theme.ClayFieldBg
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClaySecondary
import com.saurabh.mediadminapp.ui.theme.ClaySuccess
import com.saurabh.mediadminapp.ui.theme.ClayTextMuted
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary
import com.saurabh.mediadminapp.ui.theme.ClayWarning
import com.saurabh.mediadminapp.utils.ScreensState.IsApprovedUserState

// Package-level aliases used by other screens/nav files
val ClayGradient = Brush.verticalGradient(colors = listOf(Color(0xFF6C63FF), Color(0xFF48CAE4)))
val ClayCardShadow = Color(0xFF6C63FF).copy(alpha = 0.25f)

enum class FilterStatus { ALL, APPROVED, PENDING, BLOCKED }

// =============================================================================
// SHADOW ENGINE  (no Modifier.shadow() anywhere)
// =============================================================================

private fun Modifier.clayDropShadow(
    cornerRadius: Dp,
    shadowAlpha: Float = 1f,
    blurRadius: Dp = 20.dp,
    offsetX: Dp = 2.dp,
    offsetY: Dp = 8.dp,
    argbColor: Int = android.graphics.Color.argb(70, 108, 99, 255)
): Modifier = drawBehind {
    if (shadowAlpha <= 0f) return@drawBehind
    val cr = cornerRadius.toPx()
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.argb(
            ((android.graphics.Color.alpha(argbColor).toFloat() * shadowAlpha).toInt()).coerceIn(0, 255),
            android.graphics.Color.red(argbColor),
            android.graphics.Color.green(argbColor),
            android.graphics.Color.blue(argbColor)
        )
        maskFilter = android.graphics.BlurMaskFilter(
            blurRadius.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL
        )
    }
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawRoundRect(
            offsetX.toPx(), offsetY.toPx(),
            size.width - offsetX.toPx(), size.height + offsetY.toPx() * 0.55f,
            cr, cr, paint
        )
    }
}

private fun Modifier.clayCircleShadow(
    shadowAlpha: Float = 1f,
    blurRadius: Dp = 12.dp,
    offsetY: Dp = 5.dp
): Modifier = drawBehind {
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.argb(
            (55 * shadowAlpha).toInt().coerceIn(0, 255), 108, 99, 255
        )
        maskFilter = android.graphics.BlurMaskFilter(
            blurRadius.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL
        )
    }
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawCircle(
            size.width / 2f,
            size.height / 2f + offsetY.toPx(),
            (size.minDimension / 2f) - 2.dp.toPx(),
            paint
        )
    }
}

// =============================================================================
// PILLOW LIGHTING ENGINE
// 5 overlay draw layers for the inflated clay surface look
// =============================================================================

private fun Modifier.clayPillowLighting(cornerRadius: Dp): Modifier = drawWithContent {
    drawContent()
    val cr = CornerRadius(cornerRadius.toPx())
    val rim = 2.dp.toPx()

    // 1: top specular gloss
    drawRoundRect(
        brush = Brush.verticalGradient(
            0.00f to Color.White.copy(alpha = 0.82f),
            0.20f to Color.White.copy(alpha = 0.28f),
            0.42f to Color.White.copy(alpha = 0.00f)
        ),
        size = size, cornerRadius = cr
    )
    // 2: bottom contact shadow
    drawRoundRect(
        brush = Brush.verticalGradient(
            0.55f to Color.Black.copy(alpha = 0.00f),
            0.82f to Color.Black.copy(alpha = 0.07f),
            1.00f to Color.Black.copy(alpha = 0.24f)
        ),
        size = size, cornerRadius = cr
    )
    // 3: side curvature shading
    drawRoundRect(
        brush = Brush.horizontalGradient(
            0.00f to Color.Black.copy(alpha = 0.08f),
            0.10f to Color.Black.copy(alpha = 0.00f),
            0.90f to Color.Black.copy(alpha = 0.00f),
            1.00f to Color.Black.copy(alpha = 0.08f)
        ),
        size = size, cornerRadius = cr
    )
    // 4: top rim bright highlight
    drawRoundRect(
        brush = Brush.verticalGradient(
            0.00f to Color.White.copy(alpha = 1.00f),
            0.30f to Color.White.copy(alpha = 0.40f),
            0.55f to Color.White.copy(alpha = 0.00f)
        ),
        size = size, cornerRadius = cr, style = Stroke(width = rim)
    )
    // 5: bottom rim depth shadow
    drawRoundRect(
        brush = Brush.verticalGradient(
            0.50f to Color.Black.copy(alpha = 0.00f),
            0.78f to Color.Black.copy(alpha = 0.09f),
            1.00f to Color.Black.copy(alpha = 0.26f)
        ),
        size = size, cornerRadius = cr, style = Stroke(width = rim)
    )
}

// =============================================================================
// HOME SCREEN
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(viewModel: MyViewModel, navController: NavController, onMenuClick: () -> Unit,scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()) {
    Log.d("DRAWER_DEBUG", "\uD83C\uDFE0 HomeScreen: Composition Started")

    val state by viewModel.getAllUserState.collectAsState()
    val isApproved = viewModel.isApprovedUser.collectAsState()

    LaunchedEffect(Unit) {
        Log.d("DRAWER_DEBUG", "\uD83C\uDFE0 HomeScreen: LaunchedEffect(Unit) Triggered -> Fetching users")
        viewModel.getAllUsers()
    }

    LaunchedEffect(state.isLoading, state.success, state.error) {
        Log.d("DRAWER_DEBUG", "\uD83C\uDFE0 HomeScreen State Updated -> isLoading: ${state.isLoading}, isSuccess: ${state.success != null}, isError: ${state.error != null}")
    }

//    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val transparentTopBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
        titleContentColor = Color.White,
        navigationIconContentColor = Color.White,
        actionIconContentColor = Color.White
    )

    Box(modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Clay Top Bar: gradient + specular + rim ────────────────────────
            Column(modifier = Modifier
                .background(brush = Brush.horizontalGradient(listOf(ClayPrimary, ClayPrimary)))  .statusBarsPadding() // Keeps status bar always colored ClayPrimary while TopAppBar collapses beneath it
                .clipToBounds()
            ){
                TopAppBar(
                    title = {
                        Text(
                            text = "Admin Management",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.background(Color.Transparent)
                        )
                    },
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Drawer")
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = transparentTopBarColors
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = ClayGradient)
                ) {
                    // Decorative blobs
                    ClayBlob1(
                        modifier = Modifier.size(200.dp).align(Alignment.TopEnd).padding(top = 10.dp, end = 10.dp),
                        color = Color.White.copy(alpha = 0.13f)
                    )
                    ClayBlob1(
                        modifier = Modifier.size(140.dp).align(Alignment.BottomStart).padding(bottom = 50.dp, start = 10.dp),
                        color = ClayAccent.copy(alpha = 0.22f)
                    )
                    ClayBlob1(
                        modifier = Modifier.size(80.dp).align(Alignment.CenterEnd).padding(end = 20.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    )

                    when {
                        state.isLoading -> {
                            Log.d("DRAWER_DEBUG", "\uD83C\uDFE0 HomeScreen UI: Showing Loading Screen")
                            ClayLoadingScreen(modifier = Modifier.fillMaxSize())
                        }
                        state.error != null -> {
                            Log.d("DRAWER_DEBUG", "\uD83C\uDFE0 HomeScreen UI: Showing Error Screen")
                            ClayErrorScreen(
                                errorMessage = state.error.toString(),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        state.success != null -> {
                            Log.d("DRAWER_DEBUG", "\uD83C\uDFE0 HomeScreen UI: Showing UserListScreen1")
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
        }
    }
}

// =============================================================================
// USER LIST
// =============================================================================

@Composable
fun UserListScreen1(
    users: List<UserItem>,
    userApprovalState: State<Map<String, IsApprovedUserState>>,
    onApprovalToggle: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Log.d("DRAWER_DEBUG", "\uD83D\uDCDC UserListScreen1: Composition Started with ${users.size} users")
    var searchTerm by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf(FilterStatus.ALL) }

    val filteredUsers = remember(users, searchTerm, filterStatus) {
        Log.d("DRAWER_DEBUG", "\uD83D\uDCDC UserListScreen1: Filtering users (Search: '$searchTerm', Status: $filterStatus)")
        users.filter { user ->
            val matchesSearch = user.name.lowercase().contains(searchTerm.lowercase()) ||
                    (user.email ?: "").lowercase().contains(searchTerm.lowercase()) ||
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
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item { ClaySearchBar(value = searchTerm, onValueChange = { searchTerm = it }) }

        item {
            val filters = listOf(
                FilterItem(FilterStatus.ALL, "All Users", Icons.AutoMirrored.Filled.List),
                FilterItem(FilterStatus.APPROVED, "Approved", Icons.Default.CheckCircle),
                FilterItem(FilterStatus.PENDING, "Pending", Icons.Default.HourglassTop),
                FilterItem(FilterStatus.BLOCKED, "Blocked", Icons.Default.PersonOff)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                items(filters) { f ->
                    ClayFilterChip(label = f.label, selected = filterStatus == f.status, onClick = { filterStatus = f.status }, icon = f.icon)
                }
            }
        }
        item { ClayHeroOverviewCard(stats = stats) }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ClayStatTile("Approved", stats.approved.toString(), ClaySuccess, "\u2705", Modifier.weight(1f))
                ClayStatTile("Pending", stats.pending.toString(), ClayWarning, "\u23F3", Modifier.weight(1f))
                ClayStatTile("Blocked", stats.blocked.toString(), ClayAccent, "\uD83D\uDEAB", Modifier.weight(1f))
            }
        }



        if (filteredUsers.isEmpty()) {
            item { ClayEmptyState(message = "No users found", emoji = "\uD83D\uDD0D") }
        } else {
            items(filteredUsers, key = { it.user_id }) { user ->
                EachUserCard(userItem = user, userApprovalState = userApprovalState, onApprovalToggle = onApprovalToggle, navController = navController)
            }
        }
    }
}

// =============================================================================
// CLAY SEARCH BAR
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClaySearchBar(value: String, onValueChange: (String) -> Unit) {
    // 1. Updated corner radius to 7.dp
    val cr = 7.dp
    val shape = RoundedCornerShape(cr)

    Box(
        modifier = Modifier
            .fillMaxWidth() // Automatically adjusts width on screen orientation changes
            .clayDropShadow(
                cornerRadius = cr,
                blurRadius = 18.dp,
                offsetY = 8.dp,
                offsetX = 3.dp,
                argbColor = android.graphics.Color.argb(55, 108, 99, 255)
            )
            .background(brush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF8F6FF))), shape = shape)
            .drawWithContent {
                drawContent()
                val cornerRad = CornerRadius(cr.toPx())
                val rim = 1.8.dp.toPx()
                drawRoundRect(brush = Brush.verticalGradient(0f to Color.White.copy(0.88f), 0.38f to Color.White.copy(0f)), size = size, cornerRadius = cornerRad)
                drawRoundRect(brush = Brush.verticalGradient(0f to Color.White.copy(1f), 0.28f to Color.White.copy(0f)), size = size, cornerRadius = cornerRad, style = Stroke(rim))
                drawRoundRect(brush = Brush.verticalGradient(0.70f to Color.Black.copy(0f), 1f to Color.Black.copy(0.14f)), size = size, cornerRadius = cornerRad, style = Stroke(rim))
            }
            .border(1.5.dp, Color.White, shape)
            .clip(shape)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Search users by name, id...", color = ClayTextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = ClayPrimary) },
            singleLine = true,
            shape = shape,
            textStyle = TextStyle(color = ClayTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ClayTextPrimary, unfocusedTextColor = ClayTextPrimary,
                cursorColor = ClayPrimary,
                focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                focusedLeadingIconColor = ClayPrimary, unfocusedLeadingIconColor = ClayPrimary.copy(alpha = 0.6f),
                focusedPlaceholderColor = ClayTextMuted, unfocusedPlaceholderColor = ClayTextMuted
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
// =============================================================================
// HERO OVERVIEW CARD
// =============================================================================

@Composable
private fun ClayHeroOverviewCard(stats: TotalUserStats) {
    val animatedProgress by animateFloatAsState(targetValue = stats.ratio, animationSpec = tween(1000), label = "heroGauge")
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "heroScale"
    )
    val cr = 28.dp
    val shape = RoundedCornerShape(cr)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clayDropShadow(cr, blurRadius = 26.dp, offsetY = 12.dp, offsetX = 4.dp, argbColor = android.graphics.Color.argb(75, 108, 99, 255))
            .background(brush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF8F6FF), Color(0xFFEFECFF))), shape = shape)
            .clayPillowLighting(cr)
            .border(2.dp, Color.White, shape)
            .clip(shape)
            .padding(22.dp)
            .pointerInput(Unit) { detectTapGestures(onPress = { pressed = true; tryAwaitRelease(); pressed = false }) }
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Brush.horizontalGradient(listOf(ClayPrimary.copy(0.13f), ClaySecondary.copy(0.10f))))
                        .border(1.dp, ClayPrimary.copy(0.22f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text("OVERVIEW METRICS", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = ClayPrimary, letterSpacing = 1.2.sp)
                }
                Spacer(Modifier.height(12.dp))
                Text("${stats.total}", fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = ClayTextPrimary)
                Text("Registered Users", fontSize = 13.sp, color = ClayTextSecondary, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text("${(stats.ratio * 100).toInt()}% Verified Members", fontSize = 12.sp, color = ClaySuccess, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.width(20.dp))
            // Donut gauge
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clayCircleShadow(blurRadius = 14.dp, offsetY = 5.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFEDE8FF))))
                    .drawWithContent {
                        drawContent()
                        val cr2 = CornerRadius(size.width / 2f)
                        val rim2 = 1.8.dp.toPx()
                        drawRoundRect(brush = Brush.verticalGradient(0f to Color.White.copy(0.85f), 0.35f to Color.White.copy(0f)), size = size, cornerRadius = cr2)
                        drawRoundRect(brush = Brush.verticalGradient(0f to Color.White.copy(1f), 0.28f to Color.White.copy(0f)), size = size, cornerRadius = cr2, style = Stroke(rim2))
                    }
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    drawArc(color = ClayFieldBg, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(10.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(brush = Brush.sweepGradient(listOf(ClayPrimary, ClaySecondary, ClayPrimary)), startAngle = -90f, sweepAngle = 360f * animatedProgress, useCenter = false, style = Stroke(10.dp.toPx(), cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${stats.approved}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = ClayPrimary)
                    Text("ok", fontSize = 9.sp, color = ClayTextSecondary)
                }
            }
        }
    }
}

// =============================================================================
// STAT TILE
// =============================================================================

@Composable
private fun ClayStatTile(label: String, value: String, color: Color, icon: String, modifier: Modifier = Modifier) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "statScale"
    )
    val cr = 22.dp
    val shape = RoundedCornerShape(cr)
    Column(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clayDropShadow(cr, blurRadius = 18.dp, offsetY = 8.dp, offsetX = 2.dp, argbColor = android.graphics.Color.argb(65, 108, 99, 255))
            .background(brush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF8F6FF))), shape = shape)
            .clayPillowLighting(cr)
            .border(2.dp, Color.White, shape)
            .clip(shape)
            .padding(vertical = 18.dp, horizontal = 10.dp)
            .pointerInput(Unit) { detectTapGestures(onPress = { pressed = true; tryAwaitRelease(); pressed = false }) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clayCircleShadow(blurRadius = 8.dp, offsetY = 3.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.16f))
                .border(1.5.dp, color.copy(alpha = 0.32f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(icon, fontSize = 16.sp) }
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = ClayTextSecondary)
    }
}

// =============================================================================
// EACH USER CARD
// =============================================================================

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

    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cardScale"
    )
    val cr = 28.dp
    val shape = RoundedCornerShape(cr)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clayDropShadow(cr, blurRadius = 26.dp, offsetY = 11.dp, offsetX = 4.dp, argbColor = android.graphics.Color.argb(68, 108, 99, 255))
            .background(brush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF8F6FF), Color(0xFFEFECFF))), shape = shape)
            .clayPillowLighting(cr)
            .border(2.dp, Color.White, shape)
            .clip(shape)
            .padding(20.dp)
            .pointerInput(Unit) { detectTapGestures(onPress = { pressed = true; tryAwaitRelease(); pressed = false }) }
    ) {
        // Header: avatar + name + badge
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ClayAvatarBubble(initial = userItem.name.firstOrNull()?.uppercase() ?: "U")
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(userItem.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ClayTextPrimary, maxLines = 1)
                Text("ID: ${userItem.user_id.take(10)}...", fontSize = 11.sp, color = ClayTextSecondary)
            }
            Spacer(Modifier.width(8.dp))
            val (badgeText, badgeColor) = when {
                userItem.block -> "Blocked" to ClayAccent
                isApproved -> "Approved" to ClaySuccess
                else -> "Pending" to ClayWarning
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(badgeColor.copy(alpha = 0.14f))
                    .border(1.dp, badgeColor.copy(alpha = 0.35f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) { Text(badgeText, color = badgeColor, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold) }
        }

        Spacer(Modifier.height(14.dp))

        // Inset info well — recessed clay surface
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFFECE8FF), Color(0xFFF0EEFF))))
                .border(1.5.dp, ClayBorder, RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("\uD83D\uDCE7 ${userItem.email ?: "No email"}", fontSize = 12.sp, color = ClayTextPrimary, fontWeight = FontWeight.Medium)
                Text("\uD83D\uDCDE ${userItem.phone_number ?: "N/A"}", fontSize = 12.sp, color = ClayTextSecondary)
            }
            userItem.address?.takeIf { it.isNotBlank() }?.let { addr ->
                Text("\uD83D\uDCCD $addr", fontSize = 12.sp, color = ClayTextSecondary, maxLines = 1)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Footer: detail button + toggle
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            ClayDetailButton(
                text = "View Details \u2192",
                onClick = { navController.navigate(Routes.UserDetailsRoutes.invoke(userItem.user_id)) },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(16.dp))
            AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp, color = ClayPrimary)
            }
            AnimatedVisibility(visible = !isLoading, enter = fadeIn(), exit = fadeOut()) {
                Switch(
                    checked = isApproved,
                    onCheckedChange = { onApprovalToggle(userItem.user_id, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White, checkedTrackColor = ClaySuccess,
                        uncheckedThumbColor = Color.White, uncheckedTrackColor = Color(0xFFCBD5E1)
                    )
                )
            }
        }
    }
}

// =============================================================================
// CLAY AVATAR BUBBLE
// =============================================================================

@Composable
private fun ClayAvatarBubble(initial: String) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "avatarScale"
    )
    Box(
        modifier = Modifier
            .size(52.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clayCircleShadow(blurRadius = 12.dp, offsetY = 5.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFEDE9FF))))
            .drawWithContent {
                drawContent()
                val cr = CornerRadius(size.width / 2f)
                val rim = 1.8.dp.toPx()
                drawRoundRect(brush = Brush.verticalGradient(0f to Color.White.copy(0.90f), 0.35f to Color.White.copy(0f)), size = size, cornerRadius = cr)
                drawRoundRect(brush = Brush.verticalGradient(0f to Color.White.copy(1f), 0.28f to Color.White.copy(0f)), size = size, cornerRadius = cr, style = Stroke(rim))
                drawRoundRect(brush = Brush.verticalGradient(0.72f to Color.Black.copy(0f), 1f to Color.Black.copy(0.16f)), size = size, cornerRadius = cr, style = Stroke(rim))
            }
            .border(2.dp, Color.White, CircleShape)
            .pointerInput(Unit) { detectTapGestures(onPress = { pressed = true; tryAwaitRelease(); pressed = false }) },
        contentAlignment = Alignment.Center
    ) {
        Text(initial, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = ClayPrimary)
    }
}

// =============================================================================
// BLOBS + DATA CLASSES
// =============================================================================

@Composable
fun ClayBlob1(modifier: Modifier = Modifier, color: Color) {
    Box(modifier = modifier.clip(CircleShape).background(color))
}

private data class TotalUserStats(val total: Int, val approved: Int, val pending: Int, val blocked: Int, val ratio: Float)
private data class FilterItem(val status: FilterStatus, val label: String, val icon: ImageVector)
