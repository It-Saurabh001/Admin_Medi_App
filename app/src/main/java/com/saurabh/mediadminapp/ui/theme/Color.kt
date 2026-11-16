package com.saurabh.mediadminapp.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * GreenShield palette (38 colours). Full usage rules: docs/color_guide.md.
 * Never hard-code hex in screens: use MaterialTheme.colorScheme, MaterialTheme.statusColors or [Shade].
 */
object GS {
    // Green: brand / success
    val Green50 = Color(0xFFEFF9F5); val Green100 = Color(0xFFD9F2E8); val Green200 = Color(0xFFB3E5D1)
    val Green300 = Color(0xFF80D2B3); val Green400 = Color(0xFF3FB88F); val Green500 = Color(0xFF129B73)
    val Green600 = Color(0xFF0B8461); val Green700 = Color(0xFF096B50); val Green800 = Color(0xFF075340)
    val Green900 = Color(0xFF053A2D)
    // Amber: warning (low stock, near expiry)
    val Amber50 = Color(0xFFFFF8EB); val Amber100 = Color(0xFFFFEBC8); val Amber200 = Color(0xFFFFD791)
    val Amber400 = Color(0xFFE8A23A); val Amber600 = Color(0xFFB8730A); val Amber700 = Color(0xFF965C08)
    val Amber800 = Color(0xFF754806)
    // Red: danger (out of stock, expired, delete)
    val Red50 = Color(0xFFFFF1F2); val Red100 = Color(0xFFFFDDE0); val Red200 = Color(0xFFFFBFC5)
    val Red600 = Color(0xFFD13440); val Red700 = Color(0xFFB52A35); val Red800 = Color(0xFF8F1F29)
    // Teal: info (batches, movements, tags)
    val Teal50 = Color(0xFFEAF8F9); val Teal100 = Color(0xFFD0F1F3); val Teal200 = Color(0xFFA6E2E6)
    val Teal600 = Color(0xFF0F7C86); val Teal700 = Color(0xFF0C636B); val Teal800 = Color(0xFF094C52)
    // Neutral
    val White = Color(0xFFFFFFFF); val IconFill = Color(0xFFF4F9F5); val FieldFill = Color(0xFFE8F0EA)
    val Ink = Color(0xFF0F2A1F); val Muted = Color(0xFF60776A); val Outline = Color(0xFFDDE8E0)
    // Logo only (splash / logo, never UI)
    val LogoLime = Color(0xFF77C148); val LogoTeal = Color(0xFF088878); val LogoCream = Color(0xFFF8F8E8)
}

/** Normal / pressed / held shades of one colour. Same family always; never ripple (see guide). */
data class Shade(val normal: Color, val pressed: Color, val held: Color)

object Shades {
    // Light backgrounds step 50>100>200 / 100>200>300; solid 600>700>800.
    val GreenSolid = Shade(GS.Green600, GS.Green700, GS.Green800)
    val GreenRow = Shade(GS.White, GS.Green50, GS.Green100)
    val GreenChipSelected = Shade(GS.Green100, GS.Green200, GS.Green300)
    val GreenSecondaryButton = Shade(GS.White, GS.Green100, GS.Green200)
    val IconButton = Shade(GS.IconFill, GS.Green100, GS.Green200)
    val SuccessPill = Shade(GS.Green100, GS.Green200, GS.Green300)
    val AmberSolid = Shade(GS.Amber600, GS.Amber700, GS.Amber800)
    val AmberPill = Shade(GS.Amber100, GS.Amber200, GS.Amber400)
    val AmberCard = Shade(GS.Amber50, GS.Amber100, GS.Amber200)
    val AmberRow = Shade(GS.White, GS.Amber50, GS.Amber100)
    val RedSolid = Shade(GS.Red600, GS.Red700, GS.Red800)
    val RedPill = Shade(GS.Red100, GS.Red200, GS.Red200)
    val RedCard = Shade(GS.Red50, GS.Red100, GS.Red200)
    val TealTile = Shade(GS.Teal100, GS.Teal200, GS.Teal200)
    val TealCard = Shade(GS.Teal50, GS.Teal100, GS.Teal200)
    val GreenText = Shade(GS.Green600, GS.Green700, GS.Green800)
    val AmberText = Shade(GS.Amber600, GS.Amber700, GS.Amber800)
    val RedText = Shade(GS.Red600, GS.Red700, GS.Red800)
    val TealText = Shade(GS.Teal600, GS.Teal700, GS.Teal800)
}

// ── Light scheme (see docs/color_guide.md for role mapping) ─────────────────
internal val LightPrimary = GS.Green600
internal val LightOnPrimary = GS.White
internal val LightPrimaryContainer = GS.Green100
internal val LightOnPrimaryContainer = GS.Green900
internal val LightSecondary = GS.Teal600
internal val LightOnSecondary = GS.White
// M3 uses secondaryContainer for every "selected" state (FilterChip, SegmentedButton, NavigationBar/Rail
// indicator, FilledTonalButton). Guide: selected = Green 100 bg + Green 600 content. Teal info uses GS.Teal* directly.
internal val LightSecondaryContainer = GS.Green100
internal val LightOnSecondaryContainer = GS.Green600
internal val LightTertiary = GS.Amber600
internal val LightOnTertiary = GS.White
internal val LightTertiaryContainer = GS.Amber100
internal val LightOnTertiaryContainer = GS.Amber800
internal val LightError = GS.Red600
internal val LightOnError = GS.White
internal val LightErrorContainer = GS.Red100
internal val LightOnErrorContainer = GS.Red800
internal val LightBackground = GS.Green50
internal val LightOnBackground = GS.Ink
internal val LightSurface = GS.White
internal val LightOnSurface = GS.Ink
internal val LightSurfaceVariant = GS.FieldFill
internal val LightOnSurfaceVariant = GS.Muted
internal val LightOutline = GS.Outline
internal val LightOutlineVariant = GS.Outline
internal val LightSurfaceContainerLowest = GS.White
internal val LightSurfaceContainerLow = GS.Green50
internal val LightSurfaceContainer = GS.IconFill
internal val LightSurfaceContainerHigh = GS.FieldFill
internal val LightSurfaceContainerHighest = GS.Green100
internal val LightInverseSurface = GS.Ink
internal val LightInverseOnSurface = GS.Green50
internal val LightInversePrimary = GS.Green300

// ── Dark scheme ──────────────────────────────────────────────────────────────
internal val DarkPrimary = Color(0xFF6FD9C3)
internal val DarkOnPrimary = Color(0xFF00372F)
internal val DarkPrimaryContainer = Color(0xFF005046)
internal val DarkOnPrimaryContainer = Color(0xFFBDEFE3)
internal val DarkSecondary = Color(0xFFA3D49A)
internal val DarkOnSecondary = Color(0xFF0B3A0C)
internal val DarkSecondaryContainer = Color(0xFF245A28)
internal val DarkOnSecondaryContainer = Color(0xFFBFF0B5)
internal val DarkTertiary = Color(0xFFC5DF73)
internal val DarkOnTertiary = Color(0xFF2B3400)
internal val DarkTertiaryContainer = Color(0xFF3F4B00)
internal val DarkOnTertiaryContainer = Color(0xFFDFF98F)
internal val DarkError = Color(0xFFFFB4AB)
internal val DarkOnError = Color(0xFF690005)
internal val DarkErrorContainer = Color(0xFF93000A)
internal val DarkOnErrorContainer = Color(0xFFFFDAD6)
internal val DarkBackground = Color(0xFF0F1513)
internal val DarkOnBackground = Color(0xFFDFE4E0)
internal val DarkSurface = Color(0xFF0F1513)
internal val DarkOnSurface = Color(0xFFDFE4E0)
internal val DarkSurfaceVariant = Color(0xFF3F4945)
internal val DarkOnSurfaceVariant = Color(0xFFBFC9C4)
internal val DarkOutline = Color(0xFF899390)
internal val DarkOutlineVariant = Color(0xFF3F4945)
internal val DarkSurfaceContainerLowest = Color(0xFF0A0F0D)
internal val DarkSurfaceContainerLow = Color(0xFF171D1B)
internal val DarkSurfaceContainer = Color(0xFF1B2220)
internal val DarkSurfaceContainerHigh = Color(0xFF262C2A)
internal val DarkSurfaceContainerHighest = Color(0xFF313735)
internal val DarkInverseSurface = Color(0xFFDFE4E0)
internal val DarkInverseOnSurface = Color(0xFF2E312F)
internal val DarkInversePrimary = GS.Green600

// ── Logo / brand spots only (never large areas or text) ─────────────────────
object BrandColors {
    val ShieldTeal = GS.LogoTeal
    val ShieldTealLight = GS.Green400
    val CapsuleTeal = GS.Teal600
    val LeafGreen = GS.Green600
    val Lime = GS.LogoLime
    val Cream = GS.LogoCream
}

// ── Status (pending has no scheme role) ─────────────────────────────────────
internal val PendingContainerLight = GS.Amber100
internal val OnPendingContainerLight = GS.Amber800
internal val PendingContainerDark = Color(0xFF5C4000)
internal val OnPendingContainerDark = GS.Amber100
