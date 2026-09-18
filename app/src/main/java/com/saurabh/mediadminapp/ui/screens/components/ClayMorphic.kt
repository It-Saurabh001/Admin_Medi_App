package com.saurabh.mediadminapp.ui.screens.components

import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.nativeCanvas


import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.trueClaymorphic(
    cornerRadius: Dp = 24.dp,
    surfaceColor: Color = Color(0xFFFAF9FF),
    ambientGlow: Color = Color(0xFF6C63FF).copy(alpha = 0.22f),
    spotDrop: Color = Color(0xFF48CAE4).copy(alpha = 0.20f),
    elevation: Dp = 16.dp,
    highlightColor: Color = Color.White.copy(alpha = 0.90f),
    shadowColor: Color = Color(0xFF6C63FF).copy(alpha = 0.22f),
    blur: Dp = 10.dp,
    offset: Dp = 5.dp
): Modifier = this
    // 1. आउटर ड्रॉप शैडो (हवा में तैरता हुआ 3D प्रभाव)
    .shadow(
        elevation = elevation,
        shape = RoundedCornerShape(cornerRadius),
        ambientColor = ambientGlow,
        spotColor = spotDrop
    )
    .clip(RoundedCornerShape(cornerRadius))
    // 2. बेस क्ले सरफेस (हल्का 45° एंगल मैट ग्रेडिएंट)
    .background(
        brush = Brush.linearGradient(
            colors = listOf(Color.White, surfaceColor),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    )
    // 3. आउटर 3D रिम (सफेद लाइट रिफ्लेक्शन)
    .border(
        width = 1.5.dp,
        brush = Brush.linearGradient(
            colors = listOf(Color.White, Color.White.copy(alpha = 0.25f))
        ),
        shape = RoundedCornerShape(cornerRadius)
    )
    // 4. असली Figma-ग्रेड Dual Inset Shadow Engine
    .drawWithContent {
        drawContent()

        val rPx = cornerRadius.toPx()
        val blurPx = blur.toPx()
        val offsetPx = offset.toPx()

        drawIntoCanvas { canvas ->
            val cardRect = Rect(0f, 0f, size.width, size.height)
            val cardPath = Path().apply {
                addRoundRect(RoundRect(cardRect, rPx, rPx))
            }

            canvas.save()
            // शैडो सिर्फ और सिर्फ कार्ड के अंदर ही ड्रॉ होगी
            canvas.clipPath(cardPath)

            val paint = Paint().asFrameworkPaint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.FILL
                maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
            }

            // ── A. Top-Left Inner Light (सफेद 3D इनर ग्लो) ──────────────────
            paint.color = highlightColor.toArgb()
            val highlightHole = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(offsetPx, offsetPx, size.width + offsetPx, size.height + offsetPx),
                        radiusX = rPx,
                        radiusY = rPx
                    )
                )
            }
            // कार्ड से कटे हुए बाहरी हिस्से को अंदर की तरफ ब्लर प्रोजेक्ट करना
            val highlightShadowPath = Path().apply {
                op(cardPath, highlightHole, PathOperation.Difference)
            }
            canvas.nativeCanvas.drawPath(highlightShadowPath.asAndroidPath(), paint)

            // ── B. Bottom-Right Inner Dark (गहराई वाली 3D इनर शैडो) ────────
            paint.color = shadowColor.toArgb()
            val darkHole = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(-offsetPx, -offsetPx, size.width - offsetPx, size.height - offsetPx),
                        radiusX = rPx,
                        radiusY = rPx
                    )
                )
            }
            val darkShadowPath = Path().apply {
                op(cardPath, darkHole, PathOperation.Difference)
            }
            canvas.nativeCanvas.drawPath(darkShadowPath.asAndroidPath(), paint)

            canvas.restore()
        }
    }