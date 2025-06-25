package com.saurabh.mediadminapp.ui.screens.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.AddProductScreen
import com.saurabh.mediadminapp.ui.screens.HistoryScreen
import com.saurabh.mediadminapp.ui.screens.HomeScreen
import com.saurabh.mediadminapp.ui.screens.OrderDetailsScreen
import com.saurabh.mediadminapp.ui.screens.ProductScreen
import com.saurabh.mediadminapp.ui.screens.SpecificProductScreen
import com.saurabh.mediadminapp.ui.screens.UpdateUserDetailsScreen
import com.saurabh.mediadminapp.ui.screens.UserSettingScreen
import com.saurabh.mediadminapp.utils.getTopBarForRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavApp(viewModel: MyViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    var selected by remember { mutableIntStateOf(0) }       // indicate selected item
    val bottomNavItem = listOf(
        BottomNavigationItem("Dashboard", Icons.Filled.Home),
        BottomNavigationItem("Add Product", Icons.Filled.Add),
        BottomNavigationItem("Orders", Icons.Filled.ShoppingCart),
        BottomNavigationItem("Available", Icons.Filled.Menu)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = getTopBarForRoute(currentRoute,navController),
//            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    bottomNavItem.forEachIndexed { index, bottomNavItem ->

                        NavigationBarItem(
                            alwaysShowLabel = true,
                            selected = selected == index,
                            onClick = { selected = index
                                when (selected) {
                                    0 -> navController.navigate(HomeRoutes())
                                    1 -> navController.navigate(ProductRoutes())
                                    2 -> navController.navigate(OrdersRoutes())
                                    3 -> navController.navigate(HistoryRoutes())
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
            }) { innerpadding ->

            NavHost(
                navController = navController,
                startDestination = HomeRoutes.route,
                modifier = Modifier.padding(innerpadding)
            ) {
                composable(HomeRoutes.route) {
                    HomeScreen(viewModel, navController)
                }
                navigation(
                    startDestination = UserSettingsRoutes.route,
                    route = "user_setting_route"
                ) {
                    composable(
                        route = UserSettingsRoutes.route,
                        arguments = listOf(navArgument("userId") { type = NavType.StringType }))
                    { backStackEntry -> // we have to get user_id from backStackentry
                        val userId = backStackEntry.arguments?.getString("userId")!!
                        UserSettingScreen(userId, viewModel, navController)
                    }
                    composable(
                        route = UpdateUserDetailsRoutes.route,
                        arguments = listOf(navArgument ("userId"){ type = NavType.StringType }))
                    { backStackEntry->
                        val userId = backStackEntry.arguments?.getString("userId")!!
                        UpdateUserDetailsScreen(userId,viewModel,navController)
                    }
                }
//                composable<Routes.UserSettingsRoutes> { backStackEntry -> // we have to get user_id from backStackentry
//                    val user_id = backStackEntry.arguments?.getString("user_id")
//                        ?: throw IllegalStateException("user_id parameter is required")
//                    UserSettingScreen(user_id, viewModel, navController)
//                }
//                composable(ProductRoutes.route) {
//                    ProductScreen(viewModel, navController)
//                }
//                composable(route = SpecificProductRoutes.route,
//                    arguments = listOf(navArgument("productId"){type = NavType.StringType})){
//                    val productId = it.arguments?.getString("productId")!!
//                    SpecificProductScreen(productId,viewModel,navController)
//                }
                navigation(
                    startDestination = ProductRoutes.route,
                    route = "productRoutes"
                ) {
                    composable(
                        route = ProductRoutes.route)
                    {
//                        backStackEntry -> // we have to get user_id from backStackentry
//                        val userId = backStackEntry.arguments?.getString("userId")!!
                        ProductScreen(viewModel, navController)
                    }
                    composable(
                        route = SpecificProductRoutes.route,
                        arguments = listOf(navArgument ("productId"){ type = NavType.StringType }))
                    { backStackEntry->
                        val productId = backStackEntry.arguments?.getString("productId")!!
                        SpecificProductScreen(productId,viewModel,navController)
                    }
                    composable(
                        route = AddProductRoutes.route)
                    {
                        AddProductScreen(viewModel,navController)
                    }

                }




                composable(OrdersRoutes.route) {
                    OrderDetailsScreen()
                }
                composable(HistoryRoutes.route) {
                    HistoryScreen()
                }

            }
        }
    }

}

data class BottomNavigationItem(val name: String, val icon : ImageVector)


