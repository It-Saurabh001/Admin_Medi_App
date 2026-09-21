package com.saurabh.mediadminapp.ui.screens.nav

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.Admin
import com.saurabh.mediadminapp.ui.screens.AboutScreen
import com.saurabh.mediadminapp.ui.screens.AddProductScreen
import com.saurabh.mediadminapp.ui.screens.HistoryScreen
import com.saurabh.mediadminapp.ui.screens.HomeScreen
import com.saurabh.mediadminapp.ui.screens.OrderDetailsScreen
import com.saurabh.mediadminapp.ui.screens.OtpScreen
import com.saurabh.mediadminapp.ui.screens.ProductScreen
import com.saurabh.mediadminapp.ui.screens.ProfileScreen
import com.saurabh.mediadminapp.ui.screens.SettingsScreen
import com.saurabh.mediadminapp.ui.screens.SignIn
import com.saurabh.mediadminapp.ui.screens.SignUp
import com.saurabh.mediadminapp.ui.screens.SpecificOrderScreen
import com.saurabh.mediadminapp.ui.screens.SpecificProductScreen
import com.saurabh.mediadminapp.ui.screens.SplashScreen
import com.saurabh.mediadminapp.ui.screens.UpdateProductScreen
import com.saurabh.mediadminapp.ui.screens.UpdateUserDetailsScreen
import com.saurabh.mediadminapp.ui.screens.UserDetailsScreen
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClaySecondary
import com.saurabh.mediadminapp.utils.utilityFunctions.getTopBarForRoute
import kotlinx.coroutines.launch


@Composable
fun NavApp(viewModel: MyViewModel) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)    // Observe backstack changes — drives isAuthRoute, topBar, bottomBar
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isHomeScreen = currentRoute?.contains("HomeRoutes", ignoreCase = true) == true
    Log.d("TAG", "Current drawerValue: ${drawerState.currentValue}, isAnimationRunning: ${drawerState.isAnimationRunning}, currentRoute: $currentRoute")


    val bottomNavItems = remember {
        listOf(
            BottomNavigationItem("Dashboard", Icons.Filled.Home),
            BottomNavigationItem("Products", Icons.Filled.Add),
            BottomNavigationItem("Orders", Icons.Filled.ShoppingCart),
            BottomNavigationItem("History", Icons.Filled.Menu)
        )
    }

    // Login state + admin profile data
    val isLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val adminState by viewModel.getAllAdminState.collectAsState()
    val loggedInAdminId by viewModel.loggedInAdminId.collectAsState()
    val admin: Admin? = adminState.success?.admins?.find { it.admin_id == loggedInAdminId }





    // ─────────────────────────────────────────────────────────────────────────
    // isAuthRoute: true when on auth screens OR backstack not yet settled (null)
    // ?: true fallback is INTENTIONAL — keeps the drawer gesture detector from
    // initializing on Frame 0 before the first backstack entry is delivered.
    // ─────────────────────────────────────────────────────────────────────────
    val isAuthRoute = when {
        currentRoute == null -> true
        currentRoute.contains("SplashRoutes") -> true
        currentRoute.contains("SignInRoutes") -> true
        currentRoute.contains("SignUpRoutes") -> true
        currentRoute.contains("VerifyOtpRoutes") -> true
        else -> false
    }

    // Bottom-nav tab index — no PagerState, plain saveable int
    var selected by rememberSaveable { mutableIntStateOf(0) }

    val isBottomBarVisible = currentRoute?.let { route ->
        route.contains("HomeRoutes", ignoreCase = true) ||
                route.contains("productRoutes", ignoreCase = true) ||
                route.contains("OrdersRoutes", ignoreCase = true) ||
                route.contains("HistoryRoutes", ignoreCase = true)
    } ?: false


    LaunchedEffect(drawerState) {
        snapshotFlow { drawerState.currentValue }.collect { newState ->
            Log.e("DRAWER_DEBUG", "🎯 State Changed to: $newState | Route: $currentRoute")
            if (newState == DrawerValue.Open) {
                // यह स्टैक ट्रेस प्रिंट करेगा जिससे पता चलेगा कि ओपन कमांड कहाँ से फायर हुई
                Log.e("DRAWER_DEBUG", "🚨 Drawer Opened! Stack Trace:", Exception("Tracking Drawer Open"))
            }
        }
    }


    // Sync bottom-nav highlight with actual current route (handles back-nav desync)
    LaunchedEffect(currentRoute) {
        selected = when {
            currentRoute?.contains("HomeRoutes", ignoreCase = true) == true -> 0
            currentRoute?.contains("productRoutes", ignoreCase = true) == true -> 1
            currentRoute?.contains("OrdersRoutes", ignoreCase = true) == true -> 2
            currentRoute?.contains("HistoryRoutes", ignoreCase = true) == true -> 3
            else -> -1 // Profile, Settings, About, Details आदि पर कोई टैब सेलेक्ट नहीं रहेगा
        }
        if (currentRoute != null && !isHomeScreen && drawerState.isOpen) {
            Log.d("TAG", " DRAWER_DEBUGRoute changed to: $currentRoute | drawer isOpen: ${drawerState.isOpen}")
            drawerState.close()
        }

    }


    // ── 2. LAUNCHED EFFECTS TRACKER ──
    LaunchedEffect(isLoggedIn) {
        Log.d("DRAWER_DEBUG", "⚡ LaunchedEffect(isLoggedIn) triggered: isLoggedIn=$isLoggedIn, isOpen=${drawerState.isOpen}")
        if (isLoggedIn) {
            viewModel.getAllAdmin()
            if (drawerState.isOpen) {
                drawerState.close()
            }
        }
    }
    LaunchedEffect(currentRoute) {
        Log.d("DRAWER_DEBUG", "⚡ LaunchedEffect(currentRoute) triggered: Route changed to $currentRoute")
        selected = when {
            currentRoute?.contains("HomeRoutes", ignoreCase = true) == true -> 0
            currentRoute?.contains("productRoutes", ignoreCase = true) == true -> 1
            currentRoute?.contains("OrdersRoutes", ignoreCase = true) == true -> 2
            currentRoute?.contains("HistoryRoutes", ignoreCase = true) == true -> 3
            else -> -1
        }
        if (currentRoute != null && !isHomeScreen && drawerState.isOpen) {
            drawerState.close()
        }
    }
    // ── Login → Home ──────────────────────────────────────────────────────────
    // If the app finds an active session while sitting on an auth screen (e.g.
    // cold start with persisted tokens), skip to the Home graph.
    LaunchedEffect(isLoggedIn, isAuthRoute) {
        if (isLoggedIn && isAuthRoute) {
            Log.d("NAV", "Admin session active on auth screen -> forcing navigation to Home")
            navController.navigate(Routes.HomeRoutes()) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn && !isAuthRoute && currentRoute != null) {
            Log.w("NAV", "Session lost on protected route ($currentRoute) -> redirecting to SignIn")
            navController.navigate(Routes.SignInRoutes) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isHomeScreen,
        drawerContent = {

                AppDrawerContent(
                    admin = admin,
                    currentRoute = currentRoute,
                    onProfileClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(Routes.ProfileRoutes.route) { launchSingleTop = true }
                    },
                    onSettingsClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(Routes.SettingsRoutes.route) { launchSingleTop = true }
                    },
                    onAboutClick = {
                        coroutineScope.launch { drawerState.close() }
                        navController.navigate(Routes.AboutRoutes.route) { launchSingleTop = true }
                    },
                    onLogoutClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            viewModel.setAdminLoggedOut()
                        }
                    }
                )

        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.background(brush = Brush.horizontalGradient(listOf(ClayPrimary, ClaySecondary))),
            topBar = {

                if (!isAuthRoute) {
                    getTopBarForRoute(currentRoute, navController, viewModel) {
                        Log.i("TAG", " DRAWER_DEBUG TopBar hamburger/menu icon CLICKED on route: $currentRoute")
                        if (isHomeScreen) {
                            coroutineScope.launch { drawerState.open() }
                        }
                    }?.invoke()
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = isBottomBarVisible,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                    modifier = Modifier.background(Color.Transparent)
                ) {
                    AppBottomBar(
                        items = bottomNavItems,
                        selected = selected,
                        onItemSelected = { index ->
                            selected = index
                            val route = when (index) {
                                0 -> Routes.HomeRoutes()
                                1 -> Routes.ProductRoutes()
                                2 -> Routes.OrdersRoutes()
                                3 -> Routes.HistoryRoutes()
                                else -> Routes.HomeRoutes()
                            }
                            navController.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(Routes.HomeRoutes.route) { saveState = true }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            // SINGLE NAVHOST FOR ENTIRE APP
            NavHost(
                navController = navController,
                startDestination = Routes.SplashRoutes.route,
                modifier = Modifier.padding(if (isAuthRoute) PaddingValues(0.dp) else innerPadding)
            ) {
                composable(Routes.SplashRoutes.route) {
                    SplashScreen(navController, viewModel)
                }
                composable<Routes.SignInRoutes> {
                    SignIn(viewModel, navController)
                }
                composable<Routes.SignUpRoutes> {
                    SignUp(viewModel, navController)
                }
                composable<Routes.VerifyOtpRoutes> { backStackEntry ->
                    val verifyOtpRoutes: Routes.VerifyOtpRoutes = backStackEntry.toRoute()
                    OtpScreen(verifyOtpRoutes.userId ?: "", viewModel, navController)
                }

                composable(Routes.HomeRoutes.route) {
                    HomeScreen(viewModel, navController)
                }

                navigation(startDestination = Routes.ProductRoutes.route, route = "productRoutes") {
                    composable(Routes.ProductRoutes.route) { ProductScreen(viewModel, navController) }
                    composable(
                        Routes.SpecificProductRoutes.route,
                        arguments = listOf(navArgument("productId") { type = NavType.StringType })
                    ) { b -> SpecificProductScreen(b.arguments?.getString("productId")!!, viewModel, navController) }
                    composable(Routes.AddProductRoutes.route) { AddProductScreen(viewModel, navController) }
                    composable(
                        Routes.UpdateProductRoutes.route,
                        arguments = listOf(navArgument("productId") { type = NavType.StringType })
                    ) { b -> UpdateProductScreen(b.arguments?.getString("productId")!!, viewModel, navController) }
                }

                navigation(startDestination = Routes.OrdersRoutes.route, route = "OrdersRoutes") {
                    composable(Routes.OrdersRoutes.route) { OrderDetailsScreen(viewModel, navController) }
                    composable(
                        Routes.SpecificOrderRoutes.route,
                        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                    ) { b -> SpecificOrderScreen(b.arguments?.getString("orderId")!!, viewModel, navController) }
                }

                composable(Routes.HistoryRoutes.route) { HistoryScreen(viewModel, navController) }
                composable(Routes.ProfileRoutes.route) { ProfileScreen(viewModel, navController) }
                composable(Routes.SettingsRoutes.route) { SettingsScreen(viewModel, navController) }
                composable(Routes.AboutRoutes.route) { AboutScreen(viewModel, navController) }

                navigation(startDestination = Routes.UserDetailsRoutes.route, route = "user_details") {
                    composable(
                        Routes.UserDetailsRoutes.route,
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { b -> UserDetailsScreen(b.arguments?.getString("userId")!!, viewModel, navController) }
                    composable(
                        Routes.UpdateUserDetailsRoutes.route,
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { b -> UpdateUserDetailsScreen(b.arguments?.getString("userId")!!, viewModel, navController) }
                }
            }
        }
    }
}

// =============================================================================
// Bottom Navigation Bar — extracted composable for reusability
// =============================================================================
@Composable
private fun AppBottomBar(
    items: List<BottomNavigationItem>,
    selected: Int,
    onItemSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        ClayPrimary.copy(alpha = 0.08f),
                        ClaySecondary.copy(alpha = 0.08f)
                    )
                )
            )
            .background(
                brush = Brush.verticalGradient(
                    0.0f to Color.White.copy(alpha = 0.16f),
                    0.35f to Color.Transparent
                )
            )
            .drawBehind {
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.04f),
                            Color.White.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    )
    {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selected == index

                // FIX A ─ Uniform corners, 22dp = height÷2 = true capsule/oval
                // Before: asymmetric 18dp top / 12dp bottom → flat bottom edges leaked white
                // After:  uniform 22dp all sides → perfect stadium pill, no corner gaps
                val animatedCorner by animateDpAsState(
                    targetValue = if (isSelected) 22.dp else 18.dp,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "cornerAnim"
                )

                // Drives shadow fade-in/out without needing elevation change
                val animatedShadowAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "shadowAlpha"
                )

                val animatedIconTint by animateColorAsState(
                    targetValue = if (isSelected) ClayPrimary else Color.White,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "iconTintAnim"
                )

                val currentShape = RoundedCornerShape(animatedCorner) // uniform all sides

                Box(
                    modifier = Modifier
                        .height(44.dp)

                        // FIX B ─ Replace graphicsLayer { shadowElevation } with
                        //         drawBehind + BlurMaskFilter via nativeCanvas
                        //
                        // Why graphicsLayer caused white corners:
                        //   When shadowElevation > 0, Android allocates a RenderNode for the
                        //   compositing layer. That node's RECTANGULAR bounds get a white
                        //   default background during shadow pass. Your rounded content only
                        //   fills the pill area, so the four rectangular corners stay white.
                        //
                        // Why drawBehind + BlurMaskFilter doesn't:
                        //   It draws directly on the same hardware canvas as everything else.
                        //   No separate compositing layer → no white rectangle → no artifacts.
                        .drawBehind {
                            if (animatedShadowAlpha > 0f) {
                                val cr = animatedCorner.toPx()
                                drawIntoCanvas { canvas ->
                                    canvas.nativeCanvas.drawRoundRect(
                                        /* left   */ 3.dp.toPx(),
                                        /* top    */ 4.dp.toPx(),
                                        /* right  */ size.width - 3.dp.toPx(),
                                        /* bottom */ size.height + 3.dp.toPx(),
                                        /* rx     */ cr,
                                        /* ry     */ cr,
                                        android.graphics.Paint().apply {
                                            isAntiAlias = true
                                            color = android.graphics.Color.argb(
                                                (55 * animatedShadowAlpha).toInt(),
                                                0, 0, 0
                                            )
                                            maskFilter = android.graphics.BlurMaskFilter(
                                                10.dp.toPx(),
                                                android.graphics.BlurMaskFilter.Blur.NORMAL
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        // Pill base color
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFFFFFFF), // Top — direct light
                                            Color(0xFFF2F6FC), // Upper body
                                            Color(0xFFE5EBF7), // Lower body
                                            Color(0xFFD5DFEE)  // Bottom — contact zone
                                        )
                                    ),
                                    shape = currentShape
                                )
                            } else {
                                Modifier.background(
                                    color = Color(0xFF1E293B).copy(alpha = 0.70f),
                                    shape = currentShape
                                )
                            }
                        )

                        // 5-layer pillow lighting model (unchanged, still fully valid)
                        .drawWithContent {
                            drawContent()
                            if (isSelected) {
                                val cr = CornerRadius(animatedCorner.toPx())
                                val rimWidth = 1.8.dp.toPx()

                                // Layer 1 — Top Specular Highlight
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        0.00f to Color.White.copy(alpha = 0.80f),
                                        0.30f to Color.White.copy(alpha = 0.18f),
                                        0.52f to Color.White.copy(alpha = 0.00f)
                                    ),
                                    size = size,
                                    cornerRadius = cr
                                )

                                // Layer 2 — Bottom Contact Shadow
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        0.48f to Color.Black.copy(alpha = 0.00f),
                                        0.78f to Color.Black.copy(alpha = 0.06f),
                                        1.00f to Color.Black.copy(alpha = 0.22f)
                                    ),
                                    size = size,
                                    cornerRadius = cr
                                )

                                // Layer 3 — Side Curvature Shading
                                drawRoundRect(
                                    brush = Brush.horizontalGradient(
                                        0.00f to Color.Black.copy(alpha = 0.08f),
                                        0.15f to Color.Black.copy(alpha = 0.00f),
                                        0.85f to Color.Black.copy(alpha = 0.00f),
                                        1.00f to Color.Black.copy(alpha = 0.08f)
                                    ),
                                    size = size,
                                    cornerRadius = cr
                                )

                                // Layer 4 — Top Rim Highlight
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        0.00f to Color.White.copy(alpha = 1.00f),
                                        0.38f to Color.White.copy(alpha = 0.30f),
                                        0.65f to Color.White.copy(alpha = 0.00f)
                                    ),
                                    size = size,
                                    cornerRadius = cr,
                                    style = Stroke(width = rimWidth)
                                )

                                // Layer 5 — Bottom Rim Shadow
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        0.40f to Color.Black.copy(alpha = 0.00f),
                                        0.78f to Color.Black.copy(alpha = 0.08f),
                                        1.00f to Color.Black.copy(alpha = 0.22f)
                                    ),
                                    size = size,
                                    cornerRadius = cr,
                                    style = Stroke(width = rimWidth)
                                )
                            }
                        }
                        .clip(currentShape)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onItemSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = if (isSelected) 16.dp else 12.dp,
                            vertical = 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.name,
                            tint = animatedIconTint,
                            modifier = Modifier.size(20.dp)
                        )

                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn(animationSpec = tween(180, delayMillis = 40)) +
                                    expandHorizontally(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    ),
                            exit = fadeOut(animationSpec = tween(100)) +
                                    shrinkHorizontally(
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                        ) {
                            Row {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.name,
                                    color = ClayPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// Drawer Content — extracted composable for cleanliness
// =============================================================================
@Composable
private fun AppDrawerContent(
    admin: Admin?,
    currentRoute: String?,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    ModalDrawerSheet(
        drawerShape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
        drawerContainerColor = Color.White,
        modifier = Modifier.fillMaxWidth(0.82f)
    ) {
        // Gradient header card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF6C63FF), Color(0xFF48CAE4))
                    )
                )
                .padding(horizontal = 24.dp, vertical = 36.dp)
        ) {
            Column {
                val initial = admin?.name?.firstOrNull()?.toString()?.uppercase() ?: "A"
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = admin?.name ?: "Admin User",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = admin?.email ?: "admin@mediadmin.app",
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        NavigationDrawerItem(
            label = { Text("Profile", fontWeight = FontWeight.Medium) },
            selected = currentRoute?.contains("ProfileRoutes") == true,
            onClick = onProfileClick,
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            label = { Text("Settings", fontWeight = FontWeight.Medium) },
            selected = currentRoute?.contains("SettingsRoutes") == true,
            onClick = onSettingsClick,
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            label = { Text("About", fontWeight = FontWeight.Medium) },
            selected = currentRoute?.contains("AboutRoutes") == true,
            onClick = onAboutClick,
            icon = { Icon(Icons.Default.Info, contentDescription = "About") },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(
            color = Color(0xFFEEEEEE),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        NavigationDrawerItem(
            label = { Text("Logout", fontWeight = FontWeight.Medium, color = Color(0xFFE53935)) },
            selected = false,
            onClick = onLogoutClick,
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color(0xFFE53935)
                )
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

data class BottomNavigationItem(val name: String, val icon: ImageVector)
