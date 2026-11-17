package com.saurabh.mediadminapp.ui.screens.nav

import com.saurabh.mediadminapp.ui.components.shadeColor
import com.saurabh.mediadminapp.ui.theme.isLight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.background
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Surface
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import com.saurabh.mediadminapp.ui.adaptive.rememberWindowSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.NotificationImportant
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.saurabh.mediadminapp.data.v1.dto.Admin
import com.saurabh.mediadminapp.ui.screens.AboutScreen
import com.saurabh.mediadminapp.ui.screens.AdminListScreen
import com.saurabh.mediadminapp.ui.screens.StockAdjustScreen
import com.saurabh.mediadminapp.ui.screens.StockPurchaseScreen
import com.saurabh.mediadminapp.ui.screens.BatchDetailScreen
import com.saurabh.mediadminapp.ui.screens.BatchListScreen
import com.saurabh.mediadminapp.ui.screens.OrderListScreen
import com.saurabh.mediadminapp.ui.screens.SalesHistoryScreen
import com.saurabh.mediadminapp.ui.screens.AdminSignUpScreen
import com.saurabh.mediadminapp.ui.screens.BarcodeScannerScreen
import com.saurabh.mediadminapp.ui.screens.ReportsScreen
import com.saurabh.mediadminapp.ui.screens.TopProductsScreen
import com.saurabh.mediadminapp.ui.screens.AccountBlockedScreen
import com.saurabh.mediadminapp.ui.screens.ForgotPasswordScreen
import com.saurabh.mediadminapp.ui.screens.ResetPasswordScreen
import com.saurabh.mediadminapp.ui.screens.UpgradeRequiredScreen
import com.saurabh.mediadminapp.ui.screens.AddProductScreen
import com.saurabh.mediadminapp.ui.screens.OtpScreen
import androidx.compose.animation.core.FastOutSlowInEasing
import com.saurabh.mediadminapp.ui.screens.OtpFlow
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
import com.saurabh.mediadminapp.utils.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavApp(viewModel: MyViewModel) {
    // 1. Lifecycle-aware state collection
    val isLoggedIn by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()
    val adminState by viewModel.getAllAdminState.collectAsStateWithLifecycle()
    val loggedInAdminId by viewModel.loggedInAdminId.collectAsStateWithLifecycle()

    // 2. ZERO AUTH FLASH GUARD: render nothing while the token check is pending (splash stays).
    val loggedIn = isLoggedIn ?: return

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    // 3. Centralized one-time snackbar collector
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

    // 4. Memoized admin lookup
    val admin: Admin? = remember(adminState.success, loggedInAdminId) {
        adminState.success?.items?.find { it.adminId == loggedInAdminId }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()
    val drawerOpen = drawerState.targetValue == DrawerValue.Open
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // 5. Route helpers
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
            currentRoute.contains("AdminRegisterRoutes", ignoreCase = true) -> true
            currentRoute.contains("VerifyOtpRoutes", ignoreCase = true) -> true
            currentRoute.contains("VerifyEmailRoutes", ignoreCase = true) -> true
            currentRoute.contains("ForgotPasswordRoutes", ignoreCase = true) -> true
            currentRoute.contains("ResetPasswordRoutes", ignoreCase = true) -> true
            currentRoute.contains("AccountBlockedRoutes", ignoreCase = true) -> true
            else -> false
        }
    }

    // Forced update: blocks every other route (back disabled on the screen).
    val upgradeRequiredVersion by viewModel.upgradeRequiredVersion.collectAsStateWithLifecycle()
    val isUpgradeRoute = currentRoute == Routes.UpgradeRequiredRoutes.route
    LaunchedEffect(upgradeRequiredVersion, currentRoute) {
        if (upgradeRequiredVersion != null && currentRoute != null && !isUpgradeRoute) {
            navController.navigate(Routes.UpgradeRequiredRoutes.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Tab selection derived from currentRoute
    val selectedTab = remember(currentRoute) {
        when {
            currentRoute?.contains("HomeRoutes", ignoreCase = true) == true -> 0
            currentRoute?.contains("productRoutes", ignoreCase = true) == true -> 1
            currentRoute?.contains("OrdersRoutes", ignoreCase = true) == true -> 2
            currentRoute?.contains("HistoryRoutes", ignoreCase = true) == true -> 3
            else -> -1
        }
    }

    // Reset TopAppBar scroll offset & close the More sheet on route change
    LaunchedEffect(currentRoute) {
        scrollBehavior.state.heightOffset = 0f
        scrollBehavior.state.contentOffset = 0f
        drawerState.close()
    }

    // 6. Consolidated session & navigation guard
    LaunchedEffect(loggedIn, isAuthRoute, currentRoute) {
        if (isUpgradeRoute) return@LaunchedEffect
        if (loggedIn) {
            viewModel.getAllAdmin()
            if (isAuthRoute && currentRoute != null) {
                navController.navigate(Routes.HomeRoutes.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        } else {
            drawerState.snapTo(DrawerValue.Closed)
            if (!isAuthRoute && currentRoute != null) {
                navController.navigate(Routes.SignInRoutes.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    // Screens' menu buttons and the More tab open the navigation drawer.
    val openDrawer: () -> Unit = remember(isTopLevelRoute) {
        { if (isTopLevelRoute) drawerScope.launch { drawerState.open() } }
    }

    val windowInfo = rememberWindowSize()
    val tabRoutes = listOf(Routes.HomeRoutes.route, Routes.ProductRoutes.route, Routes.OrdersRoutes.route, Routes.HistoryRoutes.route)
    val onTabSelected: (Int) -> Unit = { index ->
        if (index == MORE_TAB) {
            drawerScope.launch { drawerState.open() }
        } else {
            val targetRoute = tabRoutes.getOrElse(index) { Routes.HomeRoutes.route }
            when {
                currentRoute == targetRoute -> Unit
                selectedTab == index -> navController.popBackStack(targetRoute, inclusive = false) // re-tap: pop to tab root
                else -> navController.navigate(targetRoute) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(Routes.HomeRoutes.route) { saveState = true }
                }
            }
        }
    }
    // Back from a non-Dashboard tab root goes to Dashboard, then exits.
    BackHandler(enabled = currentRoute in tabRoutes.drop(1)) {
        navController.navigate(Routes.HomeRoutes.route) {
            popUpTo(Routes.HomeRoutes.route) { inclusive = false }
            launchSingleTop = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isTopLevelRoute || drawerOpen,
        drawerContent = {
            AppDrawerSheet(
                admin = admin,
                currentRoute = currentRoute,
                onNavigate = { route ->
                    drawerScope.launch { drawerState.close() }
                    navController.navigate(route) { launchSingleTop = true }
                },
                onLogout = {
                    drawerScope.launch { drawerState.close() }
                    viewModel.setAdminLoggedOut()
                }
            )
        }
    ) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        bottomBar = {
            if (isTopLevelRoute && windowInfo.isCompact) {
                AppNavigationBar(
                    selected = if (drawerOpen) MORE_TAB else selectedTab,
                    onItemSelected = onTabSelected
                )
            }
        }
    ) { innerPadding ->
        // 7. Dynamic Start Destination: Agar pehle se loggedIn hai toh seedha Home khulega!
        // remember { } lagaya hai taaki बीच में state change hone par NavHost recreate na ho.
        val initialDestination = remember {
            if (loggedIn) Routes.HomeRoutes.route else Routes.SignInRoutes.route
        }

        Row(
            Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .consumeWindowInsets(PaddingValues(bottom = innerPadding.calculateBottomPadding()))
        ) {
        if (isTopLevelRoute && !windowInfo.isCompact) {
            AppNavigationRail(
                selected = if (drawerOpen) MORE_TAB else selectedTab,
                onItemSelected = onTabSelected
            )
        }
        NavHost(
            navController = navController,
            startDestination = initialDestination,
            modifier = Modifier.weight(1f).fillMaxHeight(),
            // Part 3 §2: forward = right->left, back = left->right, bottom-nav tabs = crossfade only.
            enterTransition = {
                if (isTabSwitch(initialState.destination.route, targetState.destination.route)) fadeIn(tween(200))
                else slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(300))
            },
            exitTransition = {
                if (isTabSwitch(initialState.destination.route, targetState.destination.route)) fadeOut(tween(200))
                else slideOutHorizontally(tween(300, easing = FastOutSlowInEasing)) { -it / 4 } + fadeOut(tween(300), targetAlpha = 0.85f)
            },
            popEnterTransition = {
                if (isTabSwitch(initialState.destination.route, targetState.destination.route)) fadeIn(tween(200))
                else slideInHorizontally(tween(280, easing = FastOutSlowInEasing)) { -it / 4 } + fadeIn(tween(280))
            },
            popExitTransition = {
                if (isTabSwitch(initialState.destination.route, targetState.destination.route)) fadeOut(tween(200))
                else slideOutHorizontally(tween(280, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(280))
            }
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
            composable(Routes.AdminRegisterRoutes.route) { AdminSignUpScreen(viewModel, navController) }
            composable(Routes.ForgotPasswordRoutes.route) {
                ForgotPasswordScreen(viewModel, navController)
            }
            composable(Routes.ResetPasswordRoutes.route) {
                ResetPasswordScreen(viewModel, navController)
            }
            composable(Routes.UpgradeRequiredRoutes.route) {
                UpgradeRequiredScreen(viewModel)
            }
            composable(Routes.AccountBlockedRoutes.route) {
                AccountBlockedScreen(viewModel, navController)
            }
            composable<Routes.VerifyOtpRoutes> { backStackEntry ->
                val verifyOtpRoutes: Routes.VerifyOtpRoutes = backStackEntry.toRoute()
                OtpScreen(OtpFlow.SignIn(verifyOtpRoutes.userId ?: ""), viewModel, navController)
            }
            composable<Routes.VerifyEmailRoutes> { backStackEntry ->
                OtpScreen(OtpFlow.EmailVerify(backStackEntry.toRoute<Routes.VerifyEmailRoutes>().email), viewModel, navController)
            }

            composable(Routes.HomeRoutes.route) {
                DashboardScreen(
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
                    OrderListScreen(
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
                SalesHistoryScreen(
                    viewModel,
                    navController,
                    onMenuClick = openDrawer,
                    scrollBehavior = scrollBehavior
                )
            }
            composable(Routes.ProfileRoutes.route) { ProfileScreen(viewModel, navController) }
            composable(Routes.SettingsRoutes.route) { SettingsScreen(viewModel, navController) }
            composable(Routes.AboutRoutes.route) { AboutScreen(viewModel, navController) }
            composable(Routes.ReportsRoutes.route) { ReportsScreen(viewModel, navController) }
            composable(Routes.TopProductsRoutes.route) { TopProductsScreen(viewModel, navController) }
            composable(Routes.BarcodeScannerRoutes.route) { BarcodeScannerScreen(viewModel, navController) }
            composable(Routes.AdminListRoutes.route) { AdminListScreen(viewModel, navController) }
            // Part 11 screens.
            composable(
                Routes.InventoryAlertsRoutes.route,
                arguments = listOf(navArgument("tab") { type = NavType.StringType; defaultValue = "LOW_STOCK" })
            ) { b ->
                com.saurabh.mediadminapp.ui.screens.InventoryAlertsScreen(b.arguments?.getString("tab"), viewModel, navController)
            }
            composable(
                Routes.StockMovementsRoutes.route,
                arguments = listOf(
                    navArgument("productId") { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument("batchId") { type = NavType.StringType; nullable = true; defaultValue = null },
                )
            ) { b ->
                com.saurabh.mediadminapp.ui.screens.StockMovementsScreen(
                    b.arguments?.getString("productId"), b.arguments?.getString("batchId"), viewModel, navController
                )
            }
            composable(Routes.SuppliersRoutes.route) { com.saurabh.mediadminapp.ui.screens.SupplierListScreen(viewModel, navController) }
            composable(
                Routes.SupplierFormRoutes.route,
                arguments = listOf(navArgument("supplierId") { type = NavType.StringType })
            ) { b ->
                val id = b.arguments?.getString("supplierId") ?: return@composable
                com.saurabh.mediadminapp.ui.screens.SupplierFormScreen(id, viewModel, navController)
            }
            composable(Routes.AuditLogRoutes.route) { com.saurabh.mediadminapp.ui.screens.AuditLogScreen(viewModel, navController) }
            // Part 10 screens (forward = right->left, back = left->right via the NavHost transitions).
            composable(Routes.UsersRoutes.route) { com.saurabh.mediadminapp.ui.screens.UserListScreen(viewModel, navController) }
            composable(
                Routes.InvoiceRoutes.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { b ->
                val id = b.arguments?.getString("orderId") ?: return@composable
                com.saurabh.mediadminapp.ui.screens.InvoiceScreen(id, viewModel, navController)
            }
            composable(
                Routes.CheckoutInvoiceRoutes.route,
                arguments = listOf(navArgument("checkoutId") { type = NavType.StringType })
            ) { b ->
                val id = b.arguments?.getString("checkoutId") ?: return@composable
                com.saurabh.mediadminapp.ui.screens.InvoiceScreen("", viewModel, navController, checkoutId = id)
            }
            composable(
                Routes.PrescriptionRoutes.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType })
            ) { b ->
                val id = b.arguments?.getString("orderId") ?: return@composable
                com.saurabh.mediadminapp.ui.screens.PrescriptionViewerScreen(id, viewModel, navController)
            }
            composable(
                Routes.AdminFormRoutes.route,
                arguments = listOf(navArgument("adminId") { type = NavType.StringType })
            ) { b ->
                val id = b.arguments?.getString("adminId") ?: return@composable
                com.saurabh.mediadminapp.ui.screens.AdminFormScreen(id, viewModel, navController)
            }
            composable(
                Routes.BatchListRoutes.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { b ->
                val id = b.arguments?.getString("productId") ?: return@composable
                BatchListScreen(id, viewModel, navController)
            }
            composable(
                Routes.BatchDetailRoutes.route,
                arguments = listOf(navArgument("batchId") { type = NavType.StringType })
            ) { b ->
                val id = b.arguments?.getString("batchId") ?: return@composable
                BatchDetailScreen(id, viewModel, navController)
            }
            composable(
                Routes.StockPurchaseRoutes.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { b ->
                val id = b.arguments?.getString("productId") ?: return@composable
                StockPurchaseScreen(id, viewModel, navController)
            }
            composable(
                Routes.StockAdjustRoutes.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { b ->
                val id = b.arguments?.getString("productId") ?: return@composable
                StockAdjustScreen(id, viewModel, navController)
            }

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

    } // ModalNavigationDrawer
}

private const val MORE_TAB = 4

private data class TopLevelTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

// "Inventory" tab from the spec has no route yet; History keeps slot 3 until it exists.
private val topLevelTabs = listOf(
    TopLevelTab("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    TopLevelTab("Products", Icons.Filled.Medication, Icons.Outlined.Medication),
    TopLevelTab("Orders", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
    TopLevelTab("History", Icons.Filled.History, Icons.Outlined.History),
    TopLevelTab("More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz),
)

// =============================================================================
// M3 NavigationBar (labels always shown, outlined/filled icon pair)
// =============================================================================
private val TAB_ROUTE_KEYS = listOf("HomeRoutes", "productRoutes", "OrdersRoutes", "HistoryRoutes")

/** True when both routes are bottom-nav tabs (crossfade, no slide). */
private fun isTabSwitch(from: String?, to: String?): Boolean =
    from != null && to != null && TAB_ROUTE_KEYS.any { from.contains(it, ignoreCase = true) } &&
        TAB_ROUTE_KEYS.any { to.contains(it, ignoreCase = true) }

@Composable
private fun AppNavigationBar(
    selected: Int,
    onItemSelected: (Int) -> Unit
) {
    // Guide §9: White bottom bar; Outline divider on top.
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
        ) {
            topLevelTabs.forEachIndexed { index, tab ->
                GsNavBarItem(
                    selected = selected == index,
                    onClick = { onItemSelected(index) },
                    selectedIcon = tab.selectedIcon,
                    unselectedIcon = tab.unselectedIcon,
                    label = tab.label,
                )
            }
        }
    }
}

/**
 * Bottom-nav item with GreenShield touch shades (no ripple):
 * selected Green 100 -> 200 (press) -> 300 (hold); unselected transparent -> Green 50 -> Green 100.
 * Filled icon when selected, Outlined otherwise; label always shown (U9).
 */
@Composable
private fun androidx.compose.foundation.layout.RowScope.GsNavBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    label: String,
) {
    val source = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val cs = MaterialTheme.colorScheme
    val light = MaterialTheme.isLight
    val shade = when {
        selected && light -> com.saurabh.mediadminapp.ui.theme.Shades.GreenChipSelected
        selected -> com.saurabh.mediadminapp.ui.theme.Shade(cs.secondaryContainer, cs.secondaryContainer, cs.secondaryContainer)
        light -> com.saurabh.mediadminapp.ui.theme.Shade(androidx.compose.ui.graphics.Color.Transparent, com.saurabh.mediadminapp.ui.theme.GS.Green50, com.saurabh.mediadminapp.ui.theme.GS.Green100)
        else -> com.saurabh.mediadminapp.ui.theme.Shade(androidx.compose.ui.graphics.Color.Transparent, cs.surfaceContainer, cs.surfaceContainerHigh)
    }
    val bg by source.shadeColor(shade, "navItem")
    val fg = if (selected) cs.onSecondaryContainer else cs.onSurfaceVariant
    Column(
        Modifier
            .weight(1f)
            // Fixed M3 bar height: the Scaffold bottomBar slot is unbounded, so fillMaxHeight()
            // here stretched the bar over the whole screen and hid the content.
            .height(80.dp)
            .clickable(
                interactionSource = source, indication = null,
                role = androidx.compose.ui.semantics.Role.Tab, onClick = onClick,
            )
            .semantics { this.selected = selected },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(width = 64.dp, height = 32.dp).clip(MaterialTheme.shapes.extraLarge).background(bg),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.animation.Crossfade(selected, animationSpec = androidx.compose.animation.core.tween(110), label = "navIcon") { sel ->
                Icon(if (sel) selectedIcon else unselectedIcon, contentDescription = null, tint = fg)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label, style = MaterialTheme.typography.labelMedium, color = fg,
            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.SemiBold else null,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AppNavigationRail(selected: Int, onItemSelected: (Int) -> Unit) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxHeight()
    ) {
        Spacer(Modifier.weight(1f))
        topLevelTabs.forEachIndexed { index, tab ->
            val isSelected = selected == index
            NavigationRailItem(
                selected = isSelected,
                onClick = { onItemSelected(index) },
                icon = { Icon(if (isSelected) tab.selectedIcon else tab.unselectedIcon, contentDescription = null) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                alwaysShowLabel = true
            )
        }
        Spacer(Modifier.weight(1f))
    }
}

// =============================================================================
// More sheet (replaces the navigation drawer; same destinations, no route change)
// =============================================================================
private data class DrawerEntry(val label: String, val icon: ImageVector, val route: String)

@Composable
private fun AppDrawerSheet(
    admin: Admin?,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
) {
    val manage = listOf(
        DrawerEntry("Reports", Icons.Outlined.Insights, Routes.ReportsRoutes.route),
        DrawerEntry("Top products", Icons.AutoMirrored.Outlined.TrendingUp, Routes.TopProductsRoutes.route),
        DrawerEntry("Scan barcode", Icons.Outlined.QrCodeScanner, Routes.BarcodeScannerRoutes.route),
        DrawerEntry("Inventory alerts", Icons.Outlined.NotificationImportant, Routes.InventoryAlertsRoutes.invoke()),
        DrawerEntry("Stock movements", Icons.Outlined.SwapVert, Routes.StockMovementsRoutes.invoke()),
        DrawerEntry("Suppliers", Icons.Outlined.LocalShipping, Routes.SuppliersRoutes.route),
        DrawerEntry("Customers", Icons.Outlined.Groups, Routes.UsersRoutes.route),
        DrawerEntry("Admins", Icons.Outlined.AdminPanelSettings, Routes.AdminListRoutes.route),
        DrawerEntry("Audit log", Icons.Outlined.History, Routes.AuditLogRoutes.route),
    )
    val account = listOf(
        DrawerEntry("Profile", Icons.Outlined.Person, Routes.ProfileRoutes.route),
        DrawerEntry("Settings", Icons.Outlined.Settings, Routes.SettingsRoutes.route),
        DrawerEntry("About", Icons.Outlined.Info, Routes.AboutRoutes.route),
    )
    ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.verticalScroll(rememberScrollState()).navigationBarsPadding()) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                com.saurabh.mediadminapp.ui.components.PersonAvatar(url = admin?.imageUrl, name = admin?.name ?: "Admin", size = 56.dp)
                Spacer(Modifier.height(8.dp))
                Text(admin?.name ?: "Admin", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                admin?.email?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline)
            Text("Manage", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 28.dp, top = 16.dp, bottom = 4.dp))
            manage.forEach { e -> DrawerRow(e, currentRoute == e.route) { onNavigate(e.route) } }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.outline)
            Text("Account", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 28.dp, top = 8.dp, bottom = 4.dp))
            account.forEach { e -> DrawerRow(e, currentRoute == e.route) { onNavigate(e.route) } }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.outline)
            NavigationDrawerItem(
                label = { Text("Log out", color = MaterialTheme.colorScheme.error) },
                icon = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                selected = false,
                onClick = onLogout,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DrawerRow(e: DrawerEntry, selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(e.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        icon = { Icon(e.icon, contentDescription = null) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}
