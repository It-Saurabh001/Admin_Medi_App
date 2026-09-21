package com.saurabh.mediadminapp.ui.screens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saurabh.mediadminapp.ui.theme.ClayBorder
import com.saurabh.mediadminapp.ui.theme.ClayButtonGradient
import com.saurabh.mediadminapp.ui.theme.ClayCardBg
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayRadiusMedium
import com.saurabh.mediadminapp.ui.theme.ClayRadiusPill
import com.saurabh.mediadminapp.ui.theme.ClayTextOnDark
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary

// =============================================================================
// ClayButton.kt — Hardware-canvas shadow buttons with pillow lighting
// =============================================================================

/**
 * Primary gradient pill button with spring press animation, BlurMaskFilter hardware
 * shadow, and loading indicator. No Modifier.shadow() to avoid RenderNode artifacts.
 */
@Composable
fun ClayPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    gradient: Brush = ClayButtonGradient,
    cornerRadius: Dp = ClayRadiusPill
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "btnPress"
    )
    val shadowAlpha by animateFloatAsState(
        targetValue = if (pressed) 0.5f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "btnShadowAlpha"
    )
    val alpha = if (enabled) 1f else 0.6f
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
            // Hardware canvas shadow — no RenderNode compositing artifacts
            .drawBehind {
                val cr = cornerRadius.toPx()
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawRoundRect(
                        4.dp.toPx(), 6.dp.toPx(),
                        size.width - 4.dp.toPx(), size.height + 4.dp.toPx(),
                        cr, cr,
                        android.graphics.Paint().apply {
                            isAntiAlias = true
                            color = android.graphics.Color.argb(
                                (65 * shadowAlpha).toInt(),
                                108, 99, 255
                            )
                            maskFilter = android.graphics.BlurMaskFilter(
                                14.dp.toPx(),
                                android.graphics.BlurMaskFilter.Blur.NORMAL
                            )
                        }
                    )
                }
            }
            .background(brush = gradient, shape = shape)
            .drawWithContent {
                drawContent()
                val cr = CornerRadius(cornerRadius.toPx())
                val rimW = 1.5.dp.toPx()
                // Top specular highlight
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        0f to Color.White.copy(alpha = 0.40f),
                        0.5f to Color.White.copy(alpha = 0f)
                    ),
                    size = size, cornerRadius = cr
                )
                // Top rim
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        0f to Color.White.copy(alpha = 0.90f),
                        0.4f to Color.White.copy(alpha = 0f)
                    ),
                    size = size, cornerRadius = cr, style = Stroke(rimW)
                )
                // Bottom rim shadow
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        0.5f to Color.Black.copy(alpha = 0f),
                        1f to Color.Black.copy(alpha = 0.15f)
                    ),
                    size = size, cornerRadius = cr, style = Stroke(rimW)
                )
            }
            .clip(shape)
            .pointerInput(enabled, onClick) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { if (!isLoading) onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = isLoading, enter = fadeIn(tween(150)), exit = fadeOut(tween(150))) {
            CircularProgressIndicator(color = ClayTextOnDark, strokeWidth = 2.5.dp, modifier = Modifier.size(24.dp))
        }
        AnimatedVisibility(visible = !isLoading, enter = fadeIn(tween(150)), exit = fadeOut(tween(150))) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ClayTextOnDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = text, color = ClayTextOnDark, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            }
        }
    }
}

/**
 * Outlined clay button — ClayCardBg surface with primary-colored border.
 * Hardware shadow replaces Modifier.shadow().
 */
@Composable
fun ClayOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    accentColor: Color = ClayPrimary,
    cornerRadius: Dp = ClayRadiusMedium
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "outlinedPress"
    )
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .height(44.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .drawBehind {
                val cr = cornerRadius.toPx()
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawRoundRect(
                        3.dp.toPx(), 4.dp.toPx(),
                        size.width - 3.dp.toPx(), size.height + 2.dp.toPx(),
                        cr, cr,
                        android.graphics.Paint().apply {
                            isAntiAlias = true
                            color = android.graphics.Color.argb(30, 108, 99, 255)
                            maskFilter = android.graphics.BlurMaskFilter(
                                8.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL
                            )
                        }
                    )
                }
            }
            .background(ClayCardBg, shape)
            .border(1.5.dp, accentColor.copy(if (enabled) 1f else 0.4f), shape)
            .clip(shape)
            .pointerInput(enabled, onClick) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (leadingIcon != null) {
                Icon(imageVector = leadingIcon, contentDescription = text, tint = accentColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = if (enabled) accentColor else accentColor.copy(0.4f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

data class FilterOption<T>(
    val key: T,
    val label: String,
    val icon: ImageVector? = null
)

/**
 * Filter chip — selected: gradient bg + white text. Unselected: clay card + primary text.
 * Spring physics on selection, hardware shadow instead of Modifier.shadow().
 */
@Composable
fun ClayFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    gradient: Brush = ClayButtonGradient
) {
    val cornerRadius by animateDpAsState(
        targetValue = if (selected) 22.dp else 18.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "chipCorner"
    )
    val shadowAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipShadow"
    )
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .wrapContentSize()
            // Hardware shadow — fires only when selected (chip is "lifted")
            .drawBehind {
                if (shadowAlpha > 0f) {
                    val cr = cornerRadius.toPx()
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawRoundRect(
                            2.dp.toPx(), 3.dp.toPx(),
                            size.width - 2.dp.toPx(), size.height + 2.dp.toPx(),
                            cr, cr,
                            android.graphics.Paint().apply {
                                isAntiAlias = true
                                color = android.graphics.Color.argb(
                                    (50 * shadowAlpha).toInt(), 108, 99, 255
                                )
                                maskFilter = android.graphics.BlurMaskFilter(
                                    8.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL
                                )
                            }
                        )
                    }
                }
            }
            .then(
                if (selected) Modifier.background(brush = gradient, shape = shape)
                else Modifier.background(ClayCardBg, shape).border(1.dp, ClayBorder, shape)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else ClayTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                color = if (selected) Color.White else ClayTextSecondary,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * Danger/destructive button (delete actions) — red-tinted gradient, same shadow model.
 */
@Composable
fun ClayDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    ClayPrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        isLoading = isLoading,
        enabled = enabled,
        gradient = Brush.horizontalGradient(
            colors = listOf(Color(0xFFEF4444), Color(0xFFFF6584))
        )
    )
}
