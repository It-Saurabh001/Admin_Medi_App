package com.saurabh.mediadminapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.saurabh.mediadminapp.data.v1.core.OrderStatus

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    inversePrimary = LightInversePrimary,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    inversePrimary = DarkInversePrimary,
)

/** Container + content pair for one order status. */
@Immutable
data class StatusColor(val container: Color, val content: Color)

/** Order status colours. Read via `MaterialTheme.statusColors`; render only via `StatusChip`. */
@Immutable
data class StatusColors(
    val pending: StatusColor,
    val approved: StatusColor,
    val sold: StatusColor,
    val cancelled: StatusColor,
    val returned: StatusColor,
) {
    fun of(status: OrderStatus): StatusColor = when (status) {
        OrderStatus.PENDING -> pending
        OrderStatus.APPROVED -> approved
        OrderStatus.SOLD -> sold
        OrderStatus.CANCELLED -> cancelled
        OrderStatus.RETURNED, OrderStatus.UNKNOWN -> returned
    }
}

private fun statusColorsFor(scheme: ColorScheme, dark: Boolean) = StatusColors(
    pending = if (dark) StatusColor(PendingContainerDark, OnPendingContainerDark)
    else StatusColor(PendingContainerLight, OnPendingContainerLight),
    approved = StatusColor(scheme.primaryContainer, scheme.onPrimaryContainer),
    sold = StatusColor(scheme.secondaryContainer, scheme.onSecondaryContainer),
    cancelled = StatusColor(scheme.errorContainer, scheme.onErrorContainer),
    returned = StatusColor(scheme.surfaceVariant, scheme.onSurfaceVariant),
)

private val LightStatusColors = statusColorsFor(LightColorScheme, dark = false)
private val DarkStatusColors = statusColorsFor(DarkColorScheme, dark = true)

val LocalStatusColors = staticCompositionLocalOf { LightStatusColors }

val MaterialTheme.statusColors: StatusColors
    @Composable @ReadOnlyComposable get() = LocalStatusColors.current

@Composable
fun MediAdminAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Brand palette only; dynamic colour is intentionally not supported.
    @Suppress("UNUSED_PARAMETER") dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val statusColors = if (darkTheme) DarkStatusColors else LightStatusColors

    CompositionLocalProvider(LocalStatusColors provides statusColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
