package com.saurabh.mediadminapp.ui.auth.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val CuraLightColorScheme = lightColorScheme(
    primary = CuraPrimary,
    onPrimary = Color.White,
    primaryContainer = CuraPrimaryContainer,
    onPrimaryContainer = CuraOnSurface,
    background = CuraBackground,
    onBackground = CuraOnSurface,
    surface = CuraSurface,
    onSurface = CuraOnSurface,
    onSurfaceVariant = CuraOnSurfaceVariant,
    outline = CuraOutline,
    error = CuraError,
    errorContainer = CuraErrorContainer,
    onError = Color.White,
)

private val CuraDarkColorScheme = darkColorScheme(
    primary = CuraPrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = CuraPrimaryContainerDark,
    onPrimaryContainer = CuraOnSurfaceDark,
    background = CuraBackgroundDark,
    onBackground = CuraOnSurfaceDark,
    surface = CuraSurfaceDark,
    onSurface = CuraOnSurfaceDark,
    onSurfaceVariant = CuraOnSurfaceVariantDark,
    outline = CuraOutlineDark,
    error = CuraErrorDark,
    errorContainer = Color(0xFF4A1515),
    onError = Color.White,
)

private val LightExtended = CuraExtendedColors(
    success = CuraSuccess,
    successContainer = CuraSuccessContainer,
    onSuccess = Color.White,
    warning = CuraWarning,
    warningContainer = CuraWarningContainer,
    onWarning = Color.White,
    info = CuraInfo,
    infoContainer = CuraInfoContainer,
    onInfo = Color.White,
)

private val DarkExtended = CuraExtendedColors(
    success = CuraSuccessDark,
    successContainer = Color(0xFF0C3020),
    onSuccess = Color.White,
    warning = CuraWarningDark,
    warningContainer = Color(0xFF3D2A08),
    onWarning = Color.Black,
    info = CuraInfoDark,
    infoContainer = Color(0xFF0F2040),
    onInfo = Color.White,
)

@Composable
fun CuraAuthTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CuraDarkColorScheme else CuraLightColorScheme
    val extended = if (darkTheme) DarkExtended else LightExtended

    CompositionLocalProvider(LocalCuraExtendedColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
