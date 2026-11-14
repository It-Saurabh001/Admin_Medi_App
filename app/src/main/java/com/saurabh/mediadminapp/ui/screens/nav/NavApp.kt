package com.saurabh.mediadminapp.ui.screens.nav

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.saurabh.mediadminapp.utils.UiEvent
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavApp(viewModel: MyViewModel) {
    // 1. Lifecycle-aware state collection
    val isLoggedIn by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()
    val adminState by viewModel.getAllAdminState.collectAsStateWithLifecycle()
    val loggedInAdminId by viewModel.loggedInAdminId.collectAsStateWithLifecycle()

    // 2. ZERO AUTH FLASH GUARD:
    // Jab tak ViewModel token check kar raha hai (null), kuch bhi render mat karo (SplashScreen ruki rahegi).
    val loggedIn = isLoggedIn ?: return

    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 3. Centralized One-Time Snackbar Collector (Poore app ke UiEvents yahan handle honge!)
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    // 4. Memoized Admin Lookup (Har recomposition par loop nahi chalega)
    val admin: Admin? = remember(adminState.success, loggedInAdminId) {
        adminState.success?.admins?.find { it.admin_id == loggedInAdminId }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val isBottomBarHidden by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction > 0.35f }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // 5. Clean Route Helpers (Single source of truth for route checks)
    val isHomeScreen = remember(currentRoute) {
        currentRoute?.contains("HomeRoutes", ignoreCase = true) == true
    }

    val isTopLevelRoute = remember(currentRoute) {
        currentRoute?.let { route ->
            route.contains("HomeRoutes", ignoreCase = true) ||
                    route.contains("productRoutes", ignoreCase = true) ||
                    route.contains("OrdersRoutes", ignoreCase = true) ||
                    route.contains("HistoryRoutes", ignoreCase = true)
        } ?: false
    }

    val isAuthRoute = remember(currentRoute) {
        when {
            currentRoute == null -> true
            currentRoute.contains("SplashRoutes", ignoreCase = true) -> true
            currentRoute.contains("SignInRoutes", ignoreCase = true) -> true
            currentRoute.contains("SignUpRoutes", ignoreCase = true) -> true
            currentRoute.contains("VerifyOtpRoutes", ignoreCase = true) -> true
            else -> false
        }
    }

    // Tab selection derived directly from currentRoute (No manual sync bugs on Back press!)
    val selectedTab = remember(currentRoute) {
        when {
            currentRoute?.contains("HomeRoutes", ignoreCase = true) == true -> 0
            currentRoute?.contains("productRoutes", ignoreCase = true) == true -> 1
            currentRoute?.contains("OrdersRoutes", ignoreCase = true) == true -> 2
            currentRoute?.contains("HistoryRoutes", ignoreCase = true) == true -> 3
            else -> -1
        }
    }

    val bottomNavItems = remember {
        listOf(
            BottomNavigationItem("Dashboard", Icons.Filled.Home),
            BottomNavigationItem("Products", Icons.Filled.Add),
            BottomNavigationItem("Orders", Icons.Filled.ShoppingCart),
            BottomNavigationItem("History", Icons.Filled.Menu)
        )
    }

    // Reset TopAppBar scroll offset & close drawer on route change
    LaunchedEffect(currentRoute) {
        scrollBehavior.state.heightOffset = 0f
        scrollBehavior.state.contentOffset = 0f
        if (currentRoute != null && !isTopLevelRoute && drawerState.isOpen) {
            drawerState.close()
        }
    }

    // 6. Consolidated Session & Navigation Guard (Sirf EK LaunchedEffect!)
    LaunchedEffect(loggedIn, isAuthRoute, currentRoute) {
        if (loggedIn) {
            viewModel.getAllAdmin()
            if (isAuthRoute && currentRoute != null) {
                navController.navigate(Routes.HomeRoutes.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        } else {
            if (drawerState.isOpen) drawerState.close()
            if (!isAuthRoute && currentRoute != null) {
                navController.navigate(Routes.SignInRoutes.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isHomeScreen, // Swipe gesture sirf Home par, par menu button sabhi main tabs par kaam karega!
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
        // Fixed: Menu icon ab Home, Products, Orders, History chaaro tabs se drawer khol sakega!
        val openDrawer: () -> Unit = remember(isTopLevelRoute) {
            {
                if (isTopLevelRoute) {
                    coroutineScope.launch { drawerState.open() }
                }
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, // Added SnackbarHost!
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .background(Color.Transparent),
            bottomBar = {
                AnimatedVisibility(
                    visible = isTopLevelRoute && !isBottomBarHidden,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    AppBottomBar(
                        items = bottomNavItems,
                        selected = selectedTab,
                        onItemSelected = { index ->
                            val targetRoute = when (index) {
                                0 -> Routes.HomeRoutes.route
                                1 -> Routes.ProductRoutes.route
                                2 -> Routes.OrdersRoutes.route
                                3 -> Routes.HistoryRoutes.route
                                else -> Routes.HomeRoutes.route
                            }
                            if (currentRoute != targetRoute) {
                                navController.navigate(targetRoute) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(Routes.HomeRoutes.route) { saveState = true }
                                }
                            }
                        }
                    )
                }
            }
        ) { _ ->
            // 7. Dynamic Start Destination: Agar pehle se loggedIn hai toh seedha Home khulega!
            // remember { } lagaya hai taaki बीच में state change hone par NavHost recreate na ho.
            val initialDestination = remember {
                if (loggedIn) Routes.HomeRoutes.route else Routes.SignInRoutes.route
            }

            NavHost(
                navController = navController,
                startDestination = initialDestination,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Routes.SplashRoutes.route) {
                    SplashScreen(navController, viewModel)
                }
                composable(Routes.SignInRoutes.route) {
                    SignIn(viewModel, navController)
                }
                composable(Routes.SignUpRoutes.route) {
                    SignUp(viewModel, navController)
                }
                composable<Routes.VerifyOtpRoutes> { backStackEntry ->
                    val verifyOtpRoutes: Routes.VerifyOtpRoutes = backStackEntry.toRoute()
                    OtpScreen(verifyOtpRoutes.userId ?: "", viewModel, navController)
                }

                composable(Routes.HomeRoutes.route) {
                    HomeScreen(
                        viewModel,
                        navController,
                        onMenuClick = openDrawer,
                        scrollBehavior = scrollBehavior
                    )
                }

                navigation(startDestination = Routes.ProductRoutes.route, route = "productRoutes") {
                    composable(Routes.ProductRoutes.route) {
                        ProductScreen(
                            viewModel,
                            navController,
                            onMenuClick = openDrawer,
                            scrollBehavior = scrollBehavior
                        )
                    }
                    composable(
                        Routes.SpecificProductRoutes.route,
                        arguments = listOf(navArgument("productId") { type = NavType.StringType })
                    ) { b ->
                        val productId = b.arguments?.getString("productId") ?: return@composable
                        SpecificProductScreen(productId, viewModel, navController)
                    }
                    composable(Routes.AddProductRoutes.route) {
                        AddProductScreen(viewModel, navController)
                    }
                    composable(
                        Routes.UpdateProductRoutes.route,
                        arguments = listOf(navArgument("productId") { type = NavType.StringType })
                    ) { b ->
                        val productId = b.arguments?.getString("productId") ?: return@composable
                        UpdateProductScreen(productId, viewModel, navController)
                    }
                }

                navigation(startDestination = Routes.OrdersRoutes.route, route = "OrdersRoutes") {
                    composable(Routes.OrdersRoutes.route) {
                        OrderDetailsScreen(
                            viewModel,
                            navController,
                            onMenuClick = openDrawer,
                            scrollBehavior = scrollBehavior
                        )
                    }
                    composable(
                        Routes.SpecificOrderRoutes.route,
                        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                    ) { b ->
                        val orderId = b.arguments?.getString("orderId") ?: return@composable
                        SpecificOrderScreen(orderId, viewModel, navController)
                    }
                }

                composable(Routes.HistoryRoutes.route) {
                    HistoryScreen(
                        viewModel,
                        navController,
                        onMenuClick = openDrawer,
                        scrollBehavior = scrollBehavior
                    )
                }
                composable(Routes.ProfileRoutes.route) { ProfileScreen(viewModel, navController) }
                composable(Routes.SettingsRoutes.route) { SettingsScreen(viewModel, navController) }
                composable(Routes.AboutRoutes.route) { AboutScreen(viewModel, navController) }

                navigation(
                    startDestination = Routes.UserDetailsRoutes.route,
                    route = "user_details"
                ) {
                    composable(
                        Routes.UserDetailsRoutes.route,
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { b ->
                        val userId = b.arguments?.getString("userId") ?: return@composable
                        UserDetailsScreen(userId, viewModel, navController)
                    }
                    composable(
                        Routes.UpdateUserDetailsRoutes.route,
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    ) { b ->
                        val userId = b.arguments?.getString("userId") ?: return@composable
                        UpdateUserDetailsScreen(userId, viewModel, navController)
                    }
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
                    colors = listOf(ClayPrimary, ClaySecondary)
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
