package com.saurabh.mediadminapp.ui.screens.nav

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
import com.saurabh.mediadminapp.ui.screens.ReportsScreen
import com.saurabh.mediadminapp.ui.screens.TopProductsScreen
import com.saurabh.mediadminapp.ui.screens.AccountBlockedScreen
import com.saurabh.mediadminapp.ui.screens.ForgotPasswordScreen
import com.saurabh.mediadminapp.ui.screens.ResetPasswordScreen
import com.saurabh.mediadminapp.ui.screens.UpgradeRequiredScreen
import com.saurabh.mediadminapp.ui.screens.AddProductScreen
import com.saurabh.mediadminapp.ui.screens.HistoryScreen
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

    var showMoreSheet by rememberSaveable { mutableStateOf(false) }
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
            currentRoute.contains("VerifyOtpRoutes", ignoreCase = true) -> true
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
        showMoreSheet = false
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
            showMoreSheet = false
            if (!isAuthRoute && currentRoute != null) {
                navController.navigate(Routes.SignInRoutes.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    // Screens' menu buttons open the More sheet (replaces the old drawer).
    val openDrawer: () -> Unit = remember(isTopLevelRoute) {
        { if (isTopLevelRoute) showMoreSheet = true }
    }

    val windowInfo = rememberWindowSize()
    val tabRoutes = listOf(Routes.HomeRoutes.route, Routes.ProductRoutes.route, Routes.OrdersRoutes.route, Routes.HistoryRoutes.route)
    val onTabSelected: (Int) -> Unit = { index ->
        if (index == MORE_TAB) {
            showMoreSheet = true
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
                    selected = if (showMoreSheet) MORE_TAB else selectedTab,
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
                selected = if (showMoreSheet) MORE_TAB else selectedTab,
                onItemSelected = onTabSelected
            )
        }
        NavHost(
            navController = navController,
            startDestination = initialDestination,
            modifier = Modifier.weight(1f).fillMaxHeight(),
            enterTransition = { fadeIn(tween(250)) + slideInHorizontally(tween(250)) { it / 12 } },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(250)) },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(250)) { it / 12 } }
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
                OtpScreen(verifyOtpRoutes.userId ?: "", viewModel, navController)
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
            composable(Routes.ReportsRoutes.route) { ReportsScreen(viewModel, navController) }
            composable(Routes.TopProductsRoutes.route) { TopProductsScreen(viewModel, navController) }

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

    if (showMoreSheet) {
        MoreSheet(
            admin = admin,
            onDismiss = { showMoreSheet = false },
            onProfileClick = {
                showMoreSheet = false
                navController.navigate(Routes.ProfileRoutes.route) { launchSingleTop = true }
            },
            onSettingsClick = {
                showMoreSheet = false
                navController.navigate(Routes.SettingsRoutes.route) { launchSingleTop = true }
            },
            onAboutClick = {
                showMoreSheet = false
                navController.navigate(Routes.AboutRoutes.route) { launchSingleTop = true }
            },
            onLogoutClick = {
                showMoreSheet = false
                viewModel.setAdminLoggedOut()
            }
        )
    }
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
@Composable
private fun AppNavigationBar(
    selected: Int,
    onItemSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        topLevelTabs.forEachIndexed { index, tab ->
            val isSelected = selected == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}

@Composable
private fun AppNavigationRail(selected: Int, onItemSelected: (Int) -> Unit) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreSheet(
    admin: Admin?,
    onDismiss: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            ListItem(
                headlineContent = {
                    Text(
                        text = admin?.name ?: "Admin",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                supportingContent = admin?.email?.let { email ->
                    {
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                leadingContent = { Icon(Icons.Outlined.Person, contentDescription = null) },
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            MoreItem("Profile", Icons.Outlined.Person, onProfileClick)
            MoreItem("Settings", Icons.Outlined.Settings, onSettingsClick)
            MoreItem("About", Icons.Outlined.Info, onAboutClick)
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            MoreItem("Logout", Icons.AutoMirrored.Outlined.Logout, onLogoutClick, isError = true)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MoreItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isError: Boolean = false
) {
    val iconColor =
        if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    val textColor =
        if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    ListItem(
        headlineContent = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = { Icon(icon, contentDescription = null) },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            headlineColor = textColor,
            leadingIconColor = iconColor
        ),
        modifier = Modifier.clickable(onClick = onClick)
    )
}

data class BottomNavigationItem(val name: String, val icon: ImageVector)
