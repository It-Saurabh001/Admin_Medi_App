package com.saurabh.mediadminapp.ui.auth.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.saurabh.mediadminapp.ui.auth.forgot.ForgotPasswordScreen
import com.saurabh.mediadminapp.ui.auth.login.LoginScreen
import com.saurabh.mediadminapp.ui.auth.otp.OtpScreen
import com.saurabh.mediadminapp.ui.auth.reset.ResetPasswordScreen
import com.saurabh.mediadminapp.ui.auth.splash.SplashScreen
import com.saurabh.mediadminapp.ui.auth.success.PasswordResetSuccessScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

private const val ANIM_DURATION = 300

// ─── NavGraphBuilder extension — embed into any NavHost ──────────────────────

/**
 * Registers all Cura auth destinations into the current [NavGraphBuilder].
 * Call this inside a `navigation(route = AuthRoutes.AUTH_GRAPH, startDestination = AuthRoutes.SPLASH)`
 * block within your existing NavHost.
 *
 * @param navController The app-level NavController.
 * @param onAuthSuccess Called when login + OTP are successfully verified — navigate to the main graph.
 */
fun NavGraphBuilder.registerAuthDestinations(
    navController: NavHostController,
    onAuthSuccess: () -> Unit,
) {
    // ── Splash ────────────────────────────────────────────────────────────────
    composable(
        route = AuthRoutes.SPLASH,
        enterTransition = { fadeIn(tween(600)) },
        exitTransition = { fadeOut(tween(400)) },
        popEnterTransition = { fadeIn(tween(600)) },
        popExitTransition = { fadeOut(tween(400)) },
    ) {
        SplashScreen(
            onNavigateToLogin = {
                navController.navigate(AuthRoutes.LOGIN) {
                    popUpTo(AuthRoutes.SPLASH) { inclusive = true }
                }
            },
            onNavigateToHome = { onAuthSuccess() }
        )
    }

    // ── Login ─────────────────────────────────────────────────────────────────
    composable(
        route = AuthRoutes.LOGIN,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(ANIM_DURATION)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(ANIM_DURATION)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(ANIM_DURATION)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(ANIM_DURATION)) },
    ) {
        LoginScreen(
            onLoginSuccess = { email ->
                navController.navigate(AuthRoutes.otp(email))
            },
            onForgotClick = {
                navController.navigate(AuthRoutes.FORGOT)
            }
        )
    }

    // ── OTP Verification ──────────────────────────────────────────────────────
    composable(
        route = AuthRoutes.OTP,
        arguments = listOf(navArgument("email") { type = NavType.StringType }),
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(ANIM_DURATION)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(ANIM_DURATION)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(ANIM_DURATION)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(ANIM_DURATION)) },
    ) { backStackEntry ->
        val raw = backStackEntry.arguments?.getString("email") ?: ""
        val email = URLDecoder.decode(raw, StandardCharsets.UTF_8.toString())
        OtpScreen(
            email = email,
            onVerified = { onAuthSuccess() },
            onBack = { navController.popBackStack() }
        )
    }

    // ── Forgot Password ───────────────────────────────────────────────────────
    composable(
        route = AuthRoutes.FORGOT,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(ANIM_DURATION)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(ANIM_DURATION)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(ANIM_DURATION)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(ANIM_DURATION)) },
    ) {
        ForgotPasswordScreen(
            onBack = { navController.popBackStack() },
            onCodeSent = { email ->
                navController.navigate(AuthRoutes.reset(email))
            }
        )
    }

    // ── Reset Password ────────────────────────────────────────────────────────
    composable(
        route = AuthRoutes.RESET,
        arguments = listOf(navArgument("email") { type = NavType.StringType }),
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(ANIM_DURATION)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(ANIM_DURATION)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(ANIM_DURATION)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(ANIM_DURATION)) },
    ) { backStackEntry ->
        val raw = backStackEntry.arguments?.getString("email") ?: ""
        val email = URLDecoder.decode(raw, StandardCharsets.UTF_8.toString())
        ResetPasswordScreen(
            email = email,
            onBack = { navController.popBackStack() },
            onSuccess = {
                navController.navigate(AuthRoutes.RESET_SUCCESS) {
                    popUpTo(AuthRoutes.FORGOT) { inclusive = true }
                }
            }
        )
    }

    // ── Password Reset Success ────────────────────────────────────────────────
    composable(
        route = AuthRoutes.RESET_SUCCESS,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(ANIM_DURATION)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(ANIM_DURATION)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(ANIM_DURATION)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(ANIM_DURATION)) },
    ) {
        PasswordResetSuccessScreen(
            onBackToLogin = {
                navController.navigate(AuthRoutes.LOGIN) {
                    popUpTo(AuthRoutes.AUTH_GRAPH) { inclusive = true }
                }
            }
        )
    }
}

// ─── Standalone NavHost variant (for use in CuraAuthActivity if needed) ──────

@Composable
fun AuthNavGraph(
    navController: NavHostController,
    onAuthSuccess: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = AuthRoutes.AUTH_GRAPH,
    ) {
        navigation(
            route = AuthRoutes.AUTH_GRAPH,
            startDestination = AuthRoutes.SPLASH
        ) {
            registerAuthDestinations(navController, onAuthSuccess)
        }
    }
}
