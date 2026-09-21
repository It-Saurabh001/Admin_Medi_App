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