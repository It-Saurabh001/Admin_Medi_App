package com.saurabh.mediadminapp.ui.screens.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.saurabh.mediadminapp.ui.theme.ClayCardBg
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayRadiusLarge
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary

// =============================================================================
// ClayCard.kt — Hardware-canvas shadow + 5-layer pillow lighting card system
// =============================================================================

/**
 * Draws an anti-aliased, BlurMaskFilter-based colored shadow directly on the
 * hardware canvas. Avoids Modifier.shadow() RenderNode compositing which leaves
 * rectangular white corners on rounded geometry.
 */
private fun Modifier.clayHardwareShadow(
    cornerRadius: Dp,
    shadowAlpha: Float = 1f,
    blurRadius: Dp = 18.dp,
    offsetY: Dp = 6.dp
): Modifier = drawBehind {
    if (shadowAlpha <= 0f) return@drawBehind
    val cr = cornerRadius.toPx()
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.drawRoundRect(
            /* left   */ 4.dp.toPx(),
            /* top    */ offsetY.toPx(),
            /* right  */ size.width - 4.dp.toPx(),
            /* bottom */ size.height + offsetY.toPx() * 0.5f,
            /* rx     */ cr,
            /* ry     */ cr,
            android.graphics.Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.argb(
                    (60 * shadowAlpha).toInt(),
                    108, 99, 255 // ClayPrimary (#6C63FF) tinted shadow
                )
                maskFilter = android.graphics.BlurMaskFilter(
                    blurRadius.toPx(),
                    android.graphics.BlurMaskFilter.Blur.NORMAL
                )
            }
        )
    }
}

/**
 * Draws 5-layer pillow lighting over content: top specular, bottom contact shadow,
 * side curvature shading, top rim highlight, bottom rim shadow.
 */
private fun Modifier.clayPillowLighting(cornerRadius: Dp): Modifier = drawWithContent {
    drawContent()
    val cr = CornerRadius(cornerRadius.toPx())
    val rimWidth = 1.8.dp.toPx()

    // Layer 1 — Top Specular Highlight
    drawRoundRect(
        brush = Brush.verticalGradient(
            0.00f to Color.White.copy(alpha = 0.72f),
            0.28f to Color.White.copy(alpha = 0.15f),
            0.50f to Color.White.copy(alpha = 0.00f)
        ),
        size = size, cornerRadius = cr
    )
    // Layer 2 — Bottom Contact Shadow
    drawRoundRect(
        brush = Brush.verticalGradient(
            0.50f to Color.Black.copy(alpha = 0.00f),
            0.80f to Color.Black.copy(alpha = 0.05f),
            1.00f to Color.Black.copy(alpha = 0.18f)
        ),
        size = size, cornerRadius = cr
    )
    // Layer 3 — Side Curvature Shading
    drawRoundRect(
        brush = Brush.horizontalGradient(
            0.00f to Color.Black.copy(alpha = 0.06f),
            0.12f to Color.Black.copy(alpha = 0.00f),
            0.88f to Color.Black.copy(alpha = 0.00f),
            1.00f to Color.Black.copy(alpha = 0.06f)
        ),
        size = size, cornerRadius = cr
    )
    // Layer 4 — Top Rim Highlight (stroke)
    drawRoundRect(
        brush = Brush.verticalGradient(
            0.00f to Color.White.copy(alpha = 1.00f),
            0.35f to Color.White.copy(alpha = 0.28f),
            0.60f to Color.White.copy(alpha = 0.00f)
        ),
        size = size, cornerRadius = cr, style = Stroke(width = rimWidth)
    )
    // Layer 5 — Bottom Rim Shadow (stroke)
    drawRoundRect(
        brush = Brush.verticalGradient(
            0.42f to Color.Black.copy(alpha = 0.00f),
            0.80f to Color.Black.copy(alpha = 0.07f),
            1.00f to Color.Black.copy(alpha = 0.20f)
        ),
        size = size, cornerRadius = cr, style = Stroke(width = rimWidth)
    )
}

/**
 * Base claymorphism card — BlurMaskFilter hardware shadow + 5-layer pillow lighting.
 * No Modifier.shadow() usage — eliminates RenderNode white corner artifacts.
 */
@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = ClayRadiusLarge,
    bgColor: Color = ClayCardBg,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .clayHardwareShadow(cornerRadius)
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFFFFFF), Color(0xFFF8F7FF), Color(0xFFF2F0FF))
                ),
                shape = shape
            )
            .clayPillowLighting(cornerRadius)
            .border(1.5.dp, Color.White, shape)
            .clip(shape)
            .padding(20.dp),
        content = content
    )
}

/**
 * Stat card — used in Home/Product summary rows.
 * Shows a large metric value, colored accent indicator, and label.
 */
@Composable
fun ClayStatCard(
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val cornerRadius = 22.dp
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .clayHardwareShadow(cornerRadius, blurRadius = 14.dp, offsetY = 4.dp)
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFFFFFF), Color(0xFFF8F7FF))
                ),
                shape = shape
            )
            .clayPillowLighting(cornerRadius)
            .border(1.5.dp, Color.White, shape)
            .clip(shape)
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = ClayTextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * A labeled info row used in detail screens (UserDetails, SpecificProduct, etc.)
 */
@Composable
fun ClayInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelColor: Color = ClayTextSecondary,
    valueColor: Color = ClayTextPrimary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = labelColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(130.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Status badge — pill-shaped with tinted background + hairline border at 25% alpha.
 */
@Composable
fun ClayStatusBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(color.copy(alpha = 0.12f))
            .border(0.5.dp, color.copy(alpha = 0.25f), RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
    }
}

/**
 * "Details" action button for list cards — spring-reactive, 44dp height,
 * BlurMaskFilter hardware shadow. Matches the 5-layer pillow lighting spec.
 */
@Composable
fun ClayDetailButton(
    text: String = "Details",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = ClayPrimary
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "detailBtnScale"
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (pressed) 14.dp else 18.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "detailBtnCorner"
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
                        2.dp.toPx(), 3.dp.toPx(),
                        size.width - 2.dp.toPx(), size.height + 2.dp.toPx(),
                        cr, cr,
                        android.graphics.Paint().apply {
                            isAntiAlias = true
                            color = android.graphics.Color.argb(50, 108, 99, 255)
                            maskFilter = android.graphics.BlurMaskFilter(
                                8.dp.toPx(),
                                android.graphics.BlurMaskFilter.Blur.NORMAL
                            )
                        }
                    )
                }
            }
            .background(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF6C63FF), Color(0xFF48CAE4))
                ),
                shape = shape
            )
            .drawWithContent {
                drawContent()
                val cr = CornerRadius(cornerRadius.toPx())
                val rimW = 1.5.dp.toPx()
                // Top specular
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        0f to Color.White.copy(0.35f), 0.5f to Color.White.copy(0f)
                    ),
                    size = size, cornerRadius = cr
                )
                // Top rim
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        0f to Color.White.copy(0.80f), 0.4f to Color.White.copy(0f)
                    ),
                    size = size, cornerRadius = cr, style = Stroke(rimW)
                )
            }
            .clip(shape)
            .pointerInput(onClick) {
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}
