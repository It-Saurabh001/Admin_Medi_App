package com.saurabh.mediadminapp.ui.auth.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object AuthRoutes {
    const val SPLASH         = "cura_splash"
    const val LOGIN          = "cura_login"
    const val OTP            = "cura_otp/{email}"
    const val FORGOT         = "cura_forgot_password"
    const val RESET          = "cura_reset_password/{email}"
    const val RESET_SUCCESS  = "cura_password_reset_success"

    // Auth graph wrapper route
    const val AUTH_GRAPH     = "cura_auth_graph"

    fun otp(email: String): String {
        val encoded = URLEncoder.encode(email, StandardCharsets.UTF_8.toString())
        return "cura_otp/$encoded"
    }

    fun reset(email: String): String {
        val encoded = URLEncoder.encode(email, StandardCharsets.UTF_8.toString())
        return "cura_reset_password/$encoded"
    }
}

/** Call this from anywhere to force-navigate to login on session expiry. */
fun navigateToLoginOnSessionExpired(navController: androidx.navigation.NavController) {
    navController.navigate(AuthRoutes.LOGIN) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}
