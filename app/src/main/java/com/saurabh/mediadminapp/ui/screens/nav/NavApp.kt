package com.saurabh.mediadminapp.ui.screens.nav

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.HomeScreen
import com.saurabh.mediadminapp.ui.screens.UserSettingScreen

@Composable
fun NavApp(viewModel: MyViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HomeRoutes
    ) {
        composable<Routes.HomeRoutes> {
            HomeScreen(viewModel,navController)
        }
        composable<Routes.UserSettingsRoutes> {backStackEntry-> // we have to get user_id from backStackentry
            val user_id = backStackEntry.arguments?.getString("user_id")?:throw IllegalStateException("user_id parameter is required")
            UserSettingScreen(user_id,viewModel,navController)
        }

    }

}


