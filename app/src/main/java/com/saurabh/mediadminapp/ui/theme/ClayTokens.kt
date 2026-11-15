package com.saurabh.mediadminapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// =============================================================================
// ClayTokens.kt — Single source of truth for the Claymorphism design system.
//
// All screens, cards, and components must consume tokens from here.
// Never hard-code colors inside screen files.
// =============================================================================

// ── Global Canvas Gradient ────────────────────────────────────────────────────
// Used on auth screens, hero headers, and list screen backdrops
val ClayGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF6C63FF), Color(0xFF48CAE4))
)

// ── Filter Pill Gradients ─────────────────────────────────────────────────────
val FilterPillGradientActive = Brush.horizontalGradient(
    colors = listOf(Color(0xFF6C63FF), Color(0xFF48CAE4))
)
val FilterPillGradientInactive = Brush.horizontalGradient(
    colors = listOf(Color.White, Color(0xFFFAF9FF))
)

// ── Spacing Tokens ────────────────────────────────────────────────────────────
val ClaySpacingScreenH = 20.dp      // Screen horizontal padding
val ClaySpacingScreenBottom = 90.dp // Bottom padding (clears nav/FAB)
val ClaySpacingCardInner = 20.dp    // Inner card padding
val ClaySpacingListGap = 18.dp      // Vertical gap between list items
val ClaySpacingRowGap = 12.dp       // Horizontal gap in tiles/pills

// ── Button Heights ────────────────────────────────────────────────────────────
val ClayButtonHeightPrimary = 54.dp  // Auth / hero primary buttons
val ClayButtonHeightSecondary = 44.dp // Secondary / inline actions

// ── Avatar ────────────────────────────────────────────────────────────────────
val ClayAvatarSize = 48.dp

// ── Backdrop Blob Sizes ───────────────────────────────────────────────────────
val ClayBlobSizeLarge = 190.dp
val ClayBlobSizeMedium = 130.dp

// ── Shadow Tint ───────────────────────────────────────────────────────────────
// Colored ambient + spot shadow — what makes it "clay" instead of grey
val ClayCardShadowAmbient = Color(0xFF6C63FF)  // ClayPrimary @ ~25% opacity used at call site
val ClayCardShadowSpot    = Color(0xFF6C63FF)  // same tint, different alpha at call site
// =============================================================================
// LEGACY Clay palette (moved from Color.kt in UI Part 0). Kept only so
// unrestyled screens still compile. Do not use in new/restyled code.
// =============================================================================
val ClayPrimary        = Color(0xFF6C63FF)
val ClayPrimaryLight   = Color(0xFF9D97FF)
val ClayPrimaryDark    = Color(0xFF4B44CC)
val ClaySecondary      = Color(0xFF48CAE4)
val ClaySecondaryLight = Color(0xFF80DFEF)
val ClayAccent         = Color(0xFFFF6584)
val ClayAccentLight    = Color(0xFFFFADBD)
val ClaySuccess        = Color(0xFF10B981)
val ClayWarning        = Color(0xFFF59E0B)
val ClayError          = Color(0xFFEF4444)
val ClayTextPrimary    = Color(0xFF1E293B)
val ClayTextSecondary  = Color(0xFF64748B)
val ClayTextOnDark     = Color(0xFFFFFFFF)
val ClayTextMuted      = Color(0xFF94A3B8)
val ClayCardBg         = Color(0xFFFAF9FF)
val ClayFieldBg        = Color(0xFFF0EEFF)
val ClayScreenBg       = Color(0xFFF5F3FF)
val ClayBorder         = Color(0xFFD0C8FF)
val ClayBorderFocused  = ClayPrimary
val ClayBadgeApproved  = ClaySuccess
val ClayBadgePending   = ClayWarning
val ClayBadgeBlocked   = ClayError
val ClayBadgeInStock   = ClaySuccess
val ClayBadgeLowStock  = ClayWarning
val ClayBadgeOutStock  = ClayError
val ClayStateApproved  = ClaySuccess
val ClayStatePending   = ClayWarning
val ClayStateBlocked   = ClayAccent
val StateApproved      = ClaySuccess
val StatePending       = ClayWarning
val StateBlocked       = ClayAccent
val ClayPrimaryGradient = Brush.horizontalGradient(listOf(ClayPrimary, ClaySecondary))
val ClayButtonGradient = Brush.horizontalGradient(listOf(Color(0xFF6C63FF), Color(0xFF48CAE4)))
val ClayHeaderGradient = Brush.verticalGradient(listOf(ClayPrimary, ClaySecondaryLight))
val ClayHomeGradient = Brush.verticalGradient(listOf(Color(0xFF6C63FF), Color(0xFF48CAE4)))
val ClayProductGradient = Brush.verticalGradient(listOf(Color(0xFF0EA5E9), Color(0xFF6C63FF)))
val ClayOrderGradient = Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF48CAE4)))
val ClayHistoryGradient = Brush.verticalGradient(listOf(Color(0xFFFF6584), Color(0xFF6C63FF)))
val ClayRadiusSmall  = 12.dp
val ClayRadiusMedium = 20.dp
val ClayRadiusLarge  = 28.dp
val ClayRadiusPill   = 50.dp
val ClayElevationCard   = 20.dp
val ClayElevationButton = 12.dp
val ClayElevationSmall  = 6.dp
val ClayChartColors = listOf(
    Color(0xFF6C63FF), Color(0xFF48CAE4), Color(0xFFFF6584),
    Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFF8B5CF6)
)
