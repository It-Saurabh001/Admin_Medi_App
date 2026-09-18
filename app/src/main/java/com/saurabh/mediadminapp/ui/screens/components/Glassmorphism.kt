package com.saurabh.mediadminapp.ui.screens.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
// Glassmorphism.kt — App-wide glass design system
// Single source of truth for: modifier, tokens, background, form primitives
// ─────────────────────────────────────────────────────────────────────────────

/**
 * App-wide glassmorphism surface modifier.
 * Every frosted panel uses this — never hardcode glass values elsewhere.
 *
 * @param shape          corner shape (28/24/20/18/16dp or CircleShape or pill)
 * @param tint           0f–1f, fill opacity (0.06 quiet → 0.22 hero)
 * @param elevation      shadow depth (4dp chip → 28dp hero). ALWAYS black, clip=false.
 * @param accentBorder   optional solid accent stroke over glass border (violet/amber/coral)
 */
fun Modifier.glassmorphic(
    shape: Shape = RoundedCornerShape(28.dp),
    tint: Float = 0.10f,
    elevation: Dp = 28.dp,
    accentBorder: Color? = null
): Modifier = composed {
    this
        .shadow(elevation = elevation, shape = shape, ambientColor = Color.Black, spotColor = Color.Black, clip = false)
        .clip(shape)
        .background(
            Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = tint + 0.08f), Color.White.copy(alpha = tint * 0.35f))
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.28f))
            ),
            shape = shape
        )
        .let { base ->
            if (accentBorder != null) base.border(width = 1.dp, color = accentBorder.copy(alpha = 0.7f), shape = shape)
            else base
        }
}

// ─────────────────────────────────────────────────────────────────────────────
// GlassTokens — locked constants, never repurpose or override
// ─────────────────────────────────────────────────────────────────────────────

object GlassTokens {
    // Background gradient (full-screen)
    val bgGradient = Brush.verticalGradient(
        listOf(Color(0xFF07081A), Color(0xFF11122B), Color(0xFF1B1240))
    )

    // Text
    val textPrimary   = Color(0xFFF5F4FF)  // titles, values, primary labels
    val textSecondary = Color(0xFFA9A7C7)  // body, icons at rest, unselected
    val textMuted     = Color(0xFF6E6C8C)  // IDs, timestamps, placeholders

    // Accents — each locked to one semantic meaning, never repurposed
    val violet = Color(0xFF8C7BFF) // approved / primary / active
    val amber  = Color(0xFFFFB648) // pending / caution
    val coral  = Color(0xFFFF5C77) // blocked / error / destructive

    // Corner radius scale
    val radiusHero   = 28.dp
    val radiusPanel  = 24.dp
    val radiusStat   = 20.dp
    val radiusField  = 18.dp
    val radiusButton = 16.dp

    // Elevation scale
    val elevationHero       = 28.dp
    val elevationStat       = 16.dp
    val elevationField      = 10.dp
    val elevationChipActive = 12.dp
    val elevationChipRest   = 4.dp
    val elevationBadge      = 6.dp
    val elevationButton     = 6.dp
}

// ─────────────────────────────────────────────────────────────────────────────
// GlassAmbientBackground — full-screen dark glass canvas with ONE shared animation
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Full-screen dark glass background with ONE shared slow-drift blob animation.
 * Wrap every glass-design screen's root Box with this.
 *
 * Rules:
 *  - This is the ONLY continuous animation allowed per screen.
 *  - Blob colors locked: violet @0.32, amber @0.16, coral @0.14.
 *  - Animation: 0→1, 16000ms, LinearEasing, RepeatMode.Reverse.
 */
@Composable
fun GlassAmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "ambientDrift")
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(animation = tween(16000, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "drift"
    )

    Box(modifier = modifier.fillMaxSize().background(brush = GlassTokens.bgGradient)) {
        // Blob A — TopStart, violet
        Box(modifier = Modifier.size(300.dp).align(Alignment.TopStart)
            .offset(x = (-60 + drift * 40).dp, y = (-60 + drift * 30).dp)
            .blur(110.dp).background(GlassTokens.violet.copy(alpha = 0.32f), CircleShape))
        // Blob B — TopEnd, amber
        Box(modifier = Modifier.size(260.dp).align(Alignment.TopEnd)
            .offset(x = (60 - drift * 40).dp, y = (-40 + drift * 20).dp)
            .blur(100.dp).background(GlassTokens.amber.copy(alpha = 0.16f), CircleShape))
        // Blob C — BottomCenter, coral
        Box(modifier = Modifier.size(320.dp).align(Alignment.BottomCenter)
            .offset(x = (-20 + drift * 40).dp, y = (80 - drift * 40).dp)
            .blur(120.dp).background(GlassTokens.coral.copy(alpha = 0.14f), CircleShape))
        // Screen content
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Glass-native form primitives
// Used by: AddProductScreen, UpdateProductScreen, UpdateUserDetailsScreen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Glass-native text input field.
 * Container is translucent glass fill; border → violet on focus.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, fontSize = 14.sp) },
        leadingIcon = leadingIcon,
        singleLine = true,
        shape = RoundedCornerShape(GlassTokens.radiusField),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = TextStyle(color = GlassTokens.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor        = GlassTokens.textPrimary,
            unfocusedTextColor      = GlassTokens.textPrimary,
            cursorColor             = GlassTokens.violet,
            focusedContainerColor   = Color.White.copy(alpha = 0.06f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
            focusedBorderColor      = GlassTokens.violet.copy(alpha = 0.7f),
            unfocusedBorderColor    = GlassTokens.textMuted.copy(alpha = 0.35f),
            focusedLabelColor       = GlassTokens.violet,
            unfocusedLabelColor     = GlassTokens.textSecondary,
            focusedPlaceholderColor   = GlassTokens.textMuted,
            unfocusedPlaceholderColor = GlassTokens.textMuted,
            focusedLeadingIconColor   = GlassTokens.violet,
            unfocusedLeadingIconColor = GlassTokens.textSecondary
        )
    )
}

/**
 * Glass primary button — glassmorphic hero tint + violet accent border + content slot.
 */
@Composable
fun GlassPrimaryButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(50.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .glassmorphic(shape = shape, tint = 0.22f, elevation = GlassTokens.elevationHero, accentBorder = GlassTokens.violet)
            .pointerInput(onClick) {
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { if (!isLoading) onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = GlassTokens.violet, strokeWidth = 2.5.dp, modifier = Modifier.size(24.dp))
        } else {
            content()
        }
    }
}

/**
 * Glass outlined button — glassmorphic surface + accent border + optional leading icon.
 */
@Composable
fun GlassOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentBorder: Color = GlassTokens.violet,
    leadingIcon: ImageVector? = null
) {
    val shape = RoundedCornerShape(GlassTokens.radiusButton)
    Box(
        modifier = modifier
            .height(44.dp)
            .glassmorphic(shape = shape, tint = 0.08f, elevation = GlassTokens.elevationBadge, accentBorder = accentBorder)
            .pointerInput(onClick) { detectTapGestures(onTap = { onClick() }) },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
            if (leadingIcon != null) {
                Icon(imageVector = leadingIcon, contentDescription = text, tint = accentBorder, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(text = text, color = accentBorder, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}