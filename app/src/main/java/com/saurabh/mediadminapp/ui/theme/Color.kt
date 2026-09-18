package com.saurabh.mediadminapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)


//── Primary Palette ──────────────────────────────────────────────────────────
val ClayPrimary        = Color(0xFF6C63FF) // Electric Violet
val ClayPrimaryLight   = Color(0xFF9D97FF)
val ClayPrimaryDark    = Color(0xFF4B44CC)

val ClaySecondary      = Color(0xFF48CAE4) // Sky Cyan
val ClaySecondaryLight = Color(0xFF80DFEF)

val ClayAccent         = Color(0xFFFF6584) // Coral Pink
val ClayAccentLight    = Color(0xFFFFADBD)

val ClaySuccess        = Color(0xFF10B981) // Emerald
val ClayWarning        = Color(0xFFF59E0B) // Amber
val ClayError          = Color(0xFFEF4444) // Red

// ── Text Colors (CRITICAL — must be high-contrast in ALL TextField states) ───
val ClayTextPrimary    = Color(0xFF1E293B) // Deep Charcoal — primary input text
val ClayTextSecondary  = Color(0xFF64748B) // Slate — placeholder / label
val ClayTextOnDark     = Color(0xFFFFFFFF) // Text on gradient surfaces
val ClayTextMuted      = Color(0xFF94A3B8) // Disabled / hint

// ── Surface & Background Colors ──────────────────────────────────────────────
val ClayCardBg         = Color(0xFFFAF9FF) // Lavender white — clay card base
val ClayFieldBg        = Color(0xFFF0EEFF) // Soft lavender — input container
val ClayScreenBg       = Color(0xFFF5F3FF) // App-wide background
val ClayBorder         = Color(0xFFD0C8FF) // Unfocused input border
val ClayBorderFocused  = ClayPrimary       // Focused input border

// ── Status Badge Colors ───────────────────────────────────────────────────────
val ClayBadgeApproved  = ClaySuccess
val ClayBadgePending   = ClayWarning
val ClayBadgeBlocked   = ClayError
val ClayBadgeInStock   = ClaySuccess
val ClayBadgeLowStock  = ClayWarning
val ClayBadgeOutStock  = ClayError

// ── Gradients ─────────────────────────────────────────────────────────────────
val ClayPrimaryGradient = Brush.horizontalGradient(
    colors = listOf(ClayPrimary, ClaySecondary)
)
val ClayButtonGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF6C63FF), Color(0xFF48CAE4))
)
val ClayHeaderGradient = Brush.verticalGradient(
    colors = listOf(ClayPrimary, ClaySecondaryLight)
)
val ClayHomeGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF6C63FF), Color(0xFF48CAE4))
)
val ClayProductGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0EA5E9), Color(0xFF6C63FF))
)
val ClayOrderGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF10B981), Color(0xFF48CAE4))
)
val ClayHistoryGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFF6584), Color(0xFF6C63FF))
)

// ── Shape Radii ───────────────────────────────────────────────────────────────
val ClayRadiusSmall  = 12.dp
val ClayRadiusMedium = 20.dp
val ClayRadiusLarge  = 28.dp
val ClayRadiusPill   = 50.dp

// ── Elevation ─────────────────────────────────────────────────────────────────
val ClayElevationCard   = 20.dp
val ClayElevationButton = 12.dp
val ClayElevationSmall  = 6.dp

// ── Donut Chart Colors ────────────────────────────────────────────────────────
val ClayChartColors = listOf(
    Color(0xFF6C63FF),
    Color(0xFF48CAE4),
    Color(0xFFFF6584),
    Color(0xFF10B981),
    Color(0xFFF59E0B),
    Color(0xFF8B5CF6)
)
