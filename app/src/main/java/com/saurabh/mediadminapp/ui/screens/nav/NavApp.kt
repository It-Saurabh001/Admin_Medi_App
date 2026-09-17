package com.saurabh.mediadminapp.ui.screens.nav

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.saurabh.mediadminapp.ui.screens.SplashScreen
import com.saurabh.mediadminapp.ui.screens.SpecificOrderScreen
import com.saurabh.mediadminapp.ui.screens.SpecificProductScreen
import com.saurabh.mediadminapp.ui.screens.UpdateProductScreen
import com.saurabh.mediadminapp.ui.screens.UpdateUserDetailsScreen
import com.saurabh.mediadminapp.ui.screens.UserDetailsScreen
import com.saurabh.mediadminapp.utils.utilityFunctions.getTopBarForRoute
import kotlinx.coroutines.launch

@Composable
fun NavApp(viewModel: MyViewModel) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    val drawerState = DrawerState(initialValue = DrawerValue.Closed)
    // Observe backstack changes — drives isAuthRoute, topBar, bottomBar
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isHomeScreen = currentRoute?.contains("HomeRoutes", ignoreCase = true) == true
    Log.d("TAG", "Current drawerValue: ${drawerState.currentValue}, isAnimationRunning: ${drawerState.isAnimationRunning}, currentRoute: $currentRoute")

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

    // Sync bottom-nav highlight with actual current route (handles back-nav desync)
    LaunchedEffect(currentRoute) {
        selected = when {
            currentRoute?.contains("HomeRoutes", ignoreCase = true) == true -> 0
            currentRoute?.contains("productRoutes", ignoreCase = true) == true -> 1
            currentRoute?.contains("OrdersRoutes", ignoreCase = true) == true -> 2
            currentRoute?.contains("HistoryRoutes", ignoreCase = true) == true -> 3
            else -> -1 // Profile, Settings, About, Details आदि पर कोई टैब सेलेक्ट नहीं रहेगा
        }
        if (!isHomeScreen && drawerState.isOpen) {
            Log.d("TAG", " DRAWER_DEBUGRoute changed to: $currentRoute | drawer isOpen: ${drawerState.isOpen}")
            drawerState.close()
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

    // Login state + admin profile data
    val isLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val adminState by viewModel.getAllAdminState.collectAsState()
    val loggedInAdminId by viewModel.loggedInAdminId.collectAsState()
    val admin: Admin? = adminState.success?.admins?.find { it.admin_id == loggedInAdminId }


    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            viewModel.getAllAdmin()
            if (drawerState.isOpen) {
                drawerState.close()
            }
        }
    }
    // ─────────────────────────────────────────────────────────────────────────
    // Auth Redirect — keyed on BOTH isLoggedIn AND currentRoute.
    // This re-fires once the backstack settles (null → real route string).
    // currentRoute != null guard prevents acting on Frame-0 null state.
    // ─────────────────────────────────────────────────────────────────────────
    LaunchedEffect(isLoggedIn, isAuthRoute) {
        if (!isLoggedIn && !isAuthRoute) {
            Log.d("NAV", "User logged out -> navigating to SignIn")
            navController.navigate(Routes.SignInRoutes) {
                popUpTo(Routes.HomeRoutes.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    LaunchedEffect(isLoggedIn, isAuthRoute) {
        if (isLoggedIn && isAuthRoute) {
            Log.d("NAV", "Admin session active on auth screen -> forcing navigation to Home")
            navController.navigate(Routes.HomeRoutes()) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // =========================================================================
    // ARCHITECTURE: Single NavHost with conditional drawer wrapper.
    //
    // Auth screens render inside a plain Box — no drawer gesture detector
    // is ever installed on the layout tree. Main screens render inside
    // ModalNavigationDrawer where the gesture detector belongs.
    //
    // We use AnimatedContent to swap between the two shells smoothly
    // while keeping a single NavController instance.
    // =========================================================================



    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isHomeScreen,
        drawerContent = {
            if (isHomeScreen) {
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
        }
    ) {
        Scaffold(
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
                    exit = slideOutVertically { it } + fadeOut()
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
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                alwaysShowLabel = true,
                selected = selected == index,
                onClick = { onItemSelected(index) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.name) },
                label = { Text(text = item.name) }
            )
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
