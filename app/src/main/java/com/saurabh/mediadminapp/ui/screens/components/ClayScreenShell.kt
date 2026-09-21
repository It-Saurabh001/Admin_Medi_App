package com.saurabh.mediadminapp.ui.screens.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saurabh.mediadminapp.ui.theme.ClayAccent
import com.saurabh.mediadminapp.ui.theme.ClayCardBg

import com.saurabh.mediadminapp.ui.theme.ClayGradient
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary

// =============================================================================
// ClayScreenShell.kt — Loading, Error, Empty state screens + backdrop helpers
// =============================================================================

/**
 * Full-screen animated loading screen with gradient background and spinner.
 * Replaces the "This is loading screen" text used everywhere previously.
 */
@Composable
fun ClayLoadingScreen(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loadingPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loadingAlpha"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .drawBehind {
                        val cr = 24.dp.toPx()
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawRoundRect(
                                4.dp.toPx(), 6.dp.toPx(),
                                size.width - 4.dp.toPx(), size.height + 4.dp.toPx(),
                                cr, cr,
                                android.graphics.Paint().apply {
                                    isAntiAlias = true
                                    color = android.graphics.Color.argb(50, 108, 99, 255)
                                    maskFilter = android.graphics.BlurMaskFilter(
                                        14.dp.toPx(),
                                        android.graphics.BlurMaskFilter.Blur.NORMAL
                                    )
                                }
                            )
                        }
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .background(ClayCardBg),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = ClayPrimary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                text = "Loading…",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = ClayTextSecondary,
                modifier = Modifier.graphicsLayer { this.alpha = alpha }
            )
        }
    }
}

/**
 * Full-screen error state with a clay card, emoji, message, and retry action.
 */
@Composable
fun ClayErrorScreen(
    errorMessage: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("⚠️", fontSize = 52.sp)
            Text(
                text = "Something went wrong",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ClayTextPrimary
            )
            Text(
                text = errorMessage,
                fontSize = 14.sp,
                color = ClayTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(8.dp))
                ClayPrimaryButton(
                    text = "Try Again",
                    onClick = onRetry,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}

/**
 * Empty state for lists with no results — e.g. no products found, no orders.
 */
@Composable
fun ClayEmptyState(
    message: String,
    subtitle: String = "",
    emoji: String = "🔍",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(emoji, fontSize = 56.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = ClayTextPrimary,
                textAlign = TextAlign.Center
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = ClayTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// =============================================================================
// ClayGradientBackdrop — Full-screen Claymorphism canvas.
//
// Draws the global ClayGradient (vertical #6C63FF → #48CAE4) behind content,
// with two decorative blobs:
//   • Top-right: White @ 12% opacity, ~190dp
//   • Bottom-left: ClayAccent (#FF6584) @ 22% opacity, ~130dp
//
// Usage: wrap the Scaffold content in this composable:
//   ClayGradientBackdrop { /* screen content */ }
// =============================================================================
@Composable
fun ClayGradientBackdrop(
    modifier: Modifier = Modifier,
    gradient: Brush = ClayGradient,
    topBlobSize: Dp = 190.dp,
    bottomBlobSize: Dp = 130.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = ClayGradient)
    ) {
        // Top-right decorative blob — white light reflection
        ClayBlobCircle(
            size = topBlobSize,
            color = Color.White.copy(alpha = 0.12f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
        )
        // Bottom-left blob — warm accent glow (clay depth)
        ClayBlobCircle(
            size = bottomBlobSize,
            color = ClayAccent.copy(alpha = 0.22f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 48.dp, start = 8.dp)
        )

        content()
    }
}

/**
 * A soft circular blob used for decorative backdrop accents.
 * Rendered as a circle with no border to keep it subtle.
 */
@Composable
fun ClayBlobCircle(
    size: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

// =============================================================================
// HorizontalScrollableText — scrollable single-line text for long product names
// used in ProductScreen's EachProductCard.
// =============================================================================
@Composable
fun HorizontalScrollableText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current
) {
    val scrollState = rememberScrollState()
    Box(modifier = modifier.horizontalScroll(scrollState)) {
        Text(
            text = text,
            style = style,
            maxLines = 1,
            softWrap = false
        )
    }
}
