package com.saurabh.mediadminapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
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

// U14 families: pending = warning (Amber 100/600), approved = info (Teal 100/600),
// sold = success (Green 100/600), cancelled = danger (Red 100/600), returned = neutral.
private fun statusColorsFor(scheme: ColorScheme, dark: Boolean) = if (dark) StatusColors(
    pending = StatusColor(PendingContainerDark, OnPendingContainerDark),
    approved = StatusColor(scheme.secondaryContainer, scheme.onSecondaryContainer),
    sold = StatusColor(scheme.primaryContainer, scheme.onPrimaryContainer),
    cancelled = StatusColor(scheme.errorContainer, scheme.onErrorContainer),
    returned = StatusColor(scheme.surfaceVariant, scheme.onSurfaceVariant),
) else StatusColors(
    pending = StatusColor(GS.Amber100, GS.Amber600),
    approved = StatusColor(GS.Teal100, GS.Teal600),
    sold = StatusColor(GS.Green100, GS.Green600),
    cancelled = StatusColor(GS.Red100, GS.Red600),
    returned = StatusColor(GS.FieldFill, GS.Muted),
)

/** True for the light (GreenShield) scheme; GS shades are light-only, dark falls back to scheme surfaces. */
val MaterialTheme.isLight: Boolean
    @Composable @ReadOnlyComposable get() = colorScheme.background.luminance() > 0.5f

/** Theme-aware press shades + control colours (rules U5/U9/U13). Use these instead of raw Shades in components. */
object GsUi {
    /** List row / tappable card: White -> Green 50 -> Green 100. */
    @Composable fun rowShade(): Shade = if (MaterialTheme.isLight) Shades.GreenRow
        else MaterialTheme.colorScheme.let { Shade(it.surfaceContainerLow, it.surfaceContainer, it.surfaceContainerHigh) }

    /** Secondary (outlined) button: White -> Green 100 -> Green 200. */
    @Composable fun secondaryButtonShade(): Shade = if (MaterialTheme.isLight) Shades.GreenSecondaryButton
        else MaterialTheme.colorScheme.let { Shade(it.surface, it.surfaceContainerHigh, it.surfaceContainerHighest) }

    /** Icon button tile (back, bell, scan): #F4F9F5 -> Green 100 -> Green 200. */
    @Composable fun iconButtonShade(): Shade = if (MaterialTheme.isLight) Shades.IconButton
        else MaterialTheme.colorScheme.let { Shade(it.surfaceContainer, it.surfaceContainerHigh, it.surfaceContainerHighest) }

    /** Light-only family shade (warning/danger cards); dark uses the row shade. */
    @Composable fun familyShade(light: Shade): Shade = if (MaterialTheme.isLight) light else rowShade()

    /** OutlinedTextField: unfocused Outline, focused 2dp Green 500, error Red 600, label Muted -> Green 600, White fill. */
    @Composable fun fieldColors(): TextFieldColors {
        val cs = MaterialTheme.colorScheme
        val light = MaterialTheme.isLight
        return OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (light) GS.Green500 else cs.primary,
            unfocusedBorderColor = cs.outline,
            errorBorderColor = cs.error,
            focusedLabelColor = cs.primary,
            unfocusedLabelColor = cs.onSurfaceVariant,
            focusedLeadingIconColor = cs.primary,
            unfocusedLeadingIconColor = cs.onSurfaceVariant,
            cursorColor = cs.primary,
            focusedContainerColor = cs.surface,
            unfocusedContainerColor = cs.surface,
            disabledContainerColor = cs.surface,
            errorContainerColor = cs.surface,
            focusedPlaceholderColor = cs.onSurfaceVariant,
            unfocusedPlaceholderColor = cs.onSurfaceVariant,
        )
    }

    /** TopAppBar: White bar, Ink title (guide §9). */
    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    @Composable fun topBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        scrolledContainerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
    )
}

private val LightStatusColors = statusColorsFor(LightColorScheme, dark = false)
private val DarkStatusColors = statusColorsFor(DarkColorScheme, dark = true)

val LocalStatusColors = staticCompositionLocalOf { LightStatusColors }

val MaterialTheme.statusColors: StatusColors
    @Composable @ReadOnlyComposable get() = LocalStatusColors.current

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MediAdminAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Brand palette only; dynamic colour is intentionally not supported.
    @Suppress("UNUSED_PARAMETER") dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val statusColors = if (darkTheme) DarkStatusColors else LightStatusColors

    // U13: no water-wave ripple anywhere; press feedback is a same-family shade change.
    CompositionLocalProvider(LocalStatusColors provides statusColors, LocalRippleConfiguration provides null) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
