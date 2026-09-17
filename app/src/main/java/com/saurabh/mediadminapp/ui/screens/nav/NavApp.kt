package com.saurabh.mediadminapp.ui.screens.nav

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.AddProductScreen
import com.saurabh.mediadminapp.ui.screens.HistoryScreen
import com.saurabh.mediadminapp.ui.screens.HomeScreen
import com.saurabh.mediadminapp.ui.screens.OrderDetailsScreen
import com.saurabh.mediadminapp.ui.screens.OrderDetailsScreen1
import com.saurabh.mediadminapp.ui.screens.ProductScreen
import com.saurabh.mediadminapp.ui.screens.SpecificOrderScreen
import com.saurabh.mediadminapp.ui.screens.SpecificProductScreen
import com.saurabh.mediadminapp.ui.screens.UpdateProductScreen
import com.saurabh.mediadminapp.ui.screens.UpdateUserDetailsScreen
import com.saurabh.mediadminapp.ui.screens.UserDetailsScreen
import com.saurabh.mediadminapp.ui.screens.SignIn
import com.saurabh.mediadminapp.ui.screens.SignUp
import com.saurabh.mediadminapp.ui.screens.OtpScreen
import com.saurabh.mediadminapp.ui.screens.SettingsScreen
import com.saurabh.mediadminapp.ui.screens.AboutScreen
import com.saurabh.mediadminapp.utils.utilityFunctions.getTopBarForRoute
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavApp(viewModel: MyViewModel) {
    Log.d("PERF_TRACE", "NavApp composition START [Thread: ${Thread.currentThread().name}]")
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var selected by rememberSaveable { mutableIntStateOf(0) }       // indicate selected item

    // remember so this list is not reallocated on every NavApp recomposition
    val bottomNavItem = remember {
        listOf(
            BottomNavigationItem("Dashboard", Icons.Filled.Home),
            BottomNavigationItem("Add Product", Icons.Filled.Add),
            BottomNavigationItem("Orders", Icons.Filled.ShoppingCart),
            BottomNavigationItem("History", Icons.Filled.Menu)
        )
    }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { bottomNavItem.size }
    )
    LaunchedEffect(pagerState.currentPage) {
        selected = pagerState.currentPage
    }

    val isLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val isAuthRoute = currentRoute?.let { route ->
        route.contains("SignInRoutes") || route.contains("SignUpRoutes") || route.contains("VerifyOtpRoutes")
    } ?: true

    val adminState by viewModel.getAllAdminState.collectAsState()
    val loggedInAdminId by viewModel.loggedInAdminId.collectAsState()
    val admin = adminState.success?.admins?.find { it.admin_id == loggedInAdminId }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            viewModel.getAllAdmin()
        }
        // Auth redirect: only re-evaluate when login state actually changes,
        // NOT on every route change — that was causing redundant navigations on every screen tap.
        if (!isLoggedIn) {
            if (!isAuthRoute && currentRoute != null) {
                Log.d("TAG", "NavApp: Redirecting to SignInRoutes (isLoggedIn=false)")
                navController.navigate(Routes.SignInRoutes) {
                    popUpTo(0) { inclusive = true }
                }
            }
        } else {
            if (isAuthRoute) {
                Log.d("TAG", "NavApp: Redirecting to HomeRoutes (isLoggedIn=true, on auth screen)")
                navController.navigate(Routes.HomeRoutes()) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    Log.d("PERF_TRACE", "ModalNavigationDrawer composition START [Thread: ${Thread.currentThread().name}]")
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isAuthRoute,
        drawerContent = {
            if (!isAuthRoute) {
                ModalDrawerSheet(
                    drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                    drawerContainerColor = Color.White,
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    // Drawer Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF4F46E5), Color(0xFF06B6D4))
                                )
                            )
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                    ) {
                        Column {
                            val initial = admin?.name?.firstOrNull()?.toString()?.uppercase() ?: "A"
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = admin?.name ?: "Admin User",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = admin?.email ?: "admin@example.com",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Drawer Items
                    NavigationDrawerItem(
                        label = { Text("Profile", fontWeight = FontWeight.Medium) },
                        selected = currentRoute?.contains("ProfileRoutes") == true,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navController.navigate(Routes.ProfileRoutes.route)
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        label = { Text("Settings", fontWeight = FontWeight.Medium) },
                        selected = currentRoute?.contains("SettingsRoutes") == true,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navController.navigate(Routes.SettingsRoutes.route)
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        label = { Text("About", fontWeight = FontWeight.Medium) },
                        selected = currentRoute?.contains("AboutRoutes") == true,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            navController.navigate(Routes.AboutRoutes.route)
                        },
                        icon = { Icon(Icons.Default.Info, contentDescription = "About") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    HorizontalDivider(
                        color = Color(0xFFF3F4F6),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    NavigationDrawerItem(
                        label = {
                            Text(
                                "Logout",
                                fontWeight = FontWeight.Medium,
                                color = Color.Red
                            )
                        },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            viewModel.setAdminLoggedOut()
                        },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.Red
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (!isAuthRoute) {
                    getTopBarForRoute(currentRoute, navController, viewModel) {
                        coroutineScope.launch {
                            drawerState.open()
                        }
                    }?.invoke()
                }
            },
            bottomBar = {
                if (!isAuthRoute) {
                    NavigationBar {
                        bottomNavItem.forEachIndexed { index, bottomNavItem ->
                            NavigationBarItem(
                                alwaysShowLabel = true,
                                selected = selected == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                    selected = index
                                    when (selected) {
                                        0 -> navController.navigate(Routes.HomeRoutes())
                                        1 -> navController.navigate(Routes.ProductRoutes())
                                        2 -> navController.navigate(Routes.OrdersRoutes())
                                        3 -> navController.navigate(Routes.HistoryRoutes())
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = bottomNavItem.icon,
                                        contentDescription = bottomNavItem.name
                                    )
                                },
                                label = { Text(text = bottomNavItem.name) }
                            )
                        }
                    }
                }
            }) { innerpadding ->

            Log.d("PERF_TRACE", "NavHost composition START [Thread: ${Thread.currentThread().name}]")
            NavHost(
                navController = navController,
                startDestination = Routes.SignInRoutes,
                modifier = Modifier.padding(innerpadding)
            ) {
                composable<Routes.SignInRoutes> {
                    Log.d("PERF_TRACE", "NavHost SignIn route composable START [Thread: ${Thread.currentThread().name}]")
                    SignIn(viewModel, navController)
                    Log.d("PERF_TRACE", "NavHost SignIn route composable END [Thread: ${Thread.currentThread().name}]")
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
                navigation(
                    startDestination = Routes.UserDetailsRoutes.route,
                    route = "user_details"
                ) {
                    composable(
                        route = Routes.UserDetailsRoutes.route,
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    )
                    { backStackEntry -> // we have to get user_id from backStackentry
                        val userId = backStackEntry.arguments?.getString("userId")!!
                        UserDetailsScreen(userId, viewModel, navController)
                    }
                    composable(
                        route = Routes.UpdateUserDetailsRoutes.route,
                        arguments = listOf(navArgument("userId") { type = NavType.StringType })
                    )
                    { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId")!!
                        UpdateUserDetailsScreen(userId, viewModel, navController)
                    }
                }
                navigation(
                    startDestination = Routes.ProductRoutes.route,
                    route = "productRoutes"
                ) {
                    composable(
                        route = Routes.ProductRoutes.route
                    )
                    {
                        ProductScreen(viewModel, navController)
                    }
                    composable(
                        route = Routes.SpecificProductRoutes.route,
                        arguments = listOf(navArgument("productId") { type = NavType.StringType })
                    )
                    { backStackEntry ->
                        val productId = backStackEntry.arguments?.getString("productId")!!
                        SpecificProductScreen(productId, viewModel, navController)
                    }
                    composable(
                        route = Routes.AddProductRoutes.route
                    )
                    {
                        AddProductScreen(viewModel, navController)
                    }
                    composable(
                        route = Routes.UpdateProductRoutes.route,
                        arguments = listOf(navArgument("productId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val productId = backStackEntry.arguments?.getString("productId")!!
                        UpdateProductScreen(productId, viewModel, navController)
                    }

                }

                navigation(
                    startDestination = Routes.OrdersRoutes.route,
                    route = "OrdersRoutes"
                ) {
                    composable(route = Routes.OrdersRoutes.route) {
                        OrderDetailsScreen(viewModel, navController)
                    }

                    composable(
                        route = Routes.SpecificOrderRoutes.route,
                        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val orderId = backStackEntry.arguments?.getString("orderId")!!
                        SpecificOrderScreen(orderId, viewModel, navController)
                    }

                }
                composable(Routes.HistoryRoutes.route) {
                    HistoryScreen(viewModel, navController)
                }
                composable(Routes.ProfileRoutes.route) {
                    ProfileScreen(viewModel, navController)
                }
                composable(Routes.SettingsRoutes.route) {
                    SettingsScreen(viewModel, navController)
                }
                composable(Routes.AboutRoutes.route) {
                    AboutScreen(viewModel, navController)
                }

            }
        }
    }
}
data class BottomNavigationItem(val name: String, val icon : ImageVector)


