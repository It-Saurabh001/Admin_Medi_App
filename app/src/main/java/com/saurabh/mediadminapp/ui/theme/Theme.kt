package com.saurabh.mediadminapp.ui.theme

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ClayPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF252139),
    onPrimaryContainer = Color(0xFFE2E8F0),
    secondary = ClaySecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1E293B),
    tertiary = ClayAccent,
    background = Color(0xFF13101C),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E1B2E),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF27233B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF4A4468),
    error = ClayError
)

private val LightColorScheme = lightColorScheme(
    primary = ClayPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF0EEFF),
    onPrimaryContainer = Color(0xFF1E293B),
    secondary = ClaySecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7FA),
    tertiary = ClayAccent,
    background = ClayScreenBg,
    onBackground = ClayTextPrimary,
    surface = ClayCardBg,
    onSurface = ClayTextPrimary,
    surfaceVariant = ClayFieldBg,
    onSurfaceVariant = ClayTextSecondary,
    outline = ClayBorder,
    error = ClayError
)

@Composable
fun MediAdminAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled by default to avoid main-thread wallpaper IPC delay on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}