package com.saurabh.mediadminapp.ui.screens.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saurabh.mediadminapp.ui.theme.ClayCardBg
import com.saurabh.mediadminapp.ui.theme.ClayChartColors
import com.saurabh.mediadminapp.ui.theme.ClayElevationCard
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayRadiusLarge
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary

// =============================================================================
// CircularAnalyticsChart.kt — Canvas-based animated donut + radial charts
//
// NO external library. Pure Compose Canvas drawArc.
// Animation: sweep angle from 0° → target on first composition (600ms ease).
// =============================================================================

/**
 * A segment of the DonutChart.
 * @param label     Legend label text
 * @param value     Numeric value (absolute, not percentage)
 * @param color     Segment arc color
 */
data class DonutSegment(
    val label: String,
    val value: Double,
    val color: Color
)

/**
 * Animated donut (ring) chart.
 *
 * - Segments animate their sweep angles from 0 → final value on first draw.
 * - Center shows a KPI callout (centerLabel + centerSubLabel).
 * - Bottom legend row shows colored dots + label + value.
 *
 * @param segments       List of DonutSegment to render
 * @param centerLabel    Bold large text in the center of the ring (e.g. "₹45,000")
 * @param centerSubLabel Smaller label below center (e.g. "Total Revenue")
 * @param chartSize      Outer size of the ring canvas
 * @param strokeWidth    Thickness of the ring
 */
@Composable
fun DonutChart(
    segments: List<DonutSegment>,
    centerLabel: String = "",
    centerSubLabel: String = "",
    modifier: Modifier = Modifier,
    chartSize: Dp = 180.dp,
    strokeWidth: Dp = 28.dp
) {
    val total = segments.sumOf { it.value }.takeIf { it > 0.0 } ?: 1.0
    var animated by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animated = true }

    // Per-segment sweep angle animation
    val sweepAngles = segments.map { seg ->
        animateFloatAsState(
            targetValue = if (animated) (seg.value / total * 360f).toFloat() else 0f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            label = "sweep_${seg.label}"
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(chartSize)
        ) {
            Canvas(modifier = Modifier.size(chartSize)) {
                val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                val diameter = size.minDimension - strokeWidth.toPx()
                val topLeft = Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                )
                val arcSize = Size(diameter, diameter)

                // Background track
                drawArc(
                    color = Color(0xFFE8E4FF),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke
                )

                // Draw each segment consecutively
                var startAngle = -90f
                sweepAngles.forEachIndexed { i, sweepState ->
                    val sweep = sweepState.value
                    if (sweep > 0f) {
                        drawArc(
                            color = segments[i].color,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = stroke
                        )
                        startAngle += sweep
                    }
                }
            }

            // Center label
            if (centerLabel.isNotEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = centerLabel,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ClayTextPrimary,
                        textAlign = TextAlign.Center
                    )
                    if (centerSubLabel.isNotEmpty()) {
                        Text(
                            text = centerSubLabel,
                            fontSize = 10.sp,
                            color = ClayTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        segments.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                pair.forEach { seg ->
                    DonutLegendItem(label = seg.label, value = seg.value, color = seg.color)
                }
                // Pad if odd segment count
                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun DonutLegendItem(label: String, value: Double, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = ClayTextSecondary, fontWeight = FontWeight.Medium)
            Text(
                text = if (value == value.toLong().toDouble()) value.toLong().toString()
                       else String.format("%.1f", value),
                fontSize = 12.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}



/**
 * Convenience: build donut segments from user stats map.
 */
fun buildUserStatsSegments(stats: Map<String, Int>): List<DonutSegment> = listOf(
    DonutSegment("Approved", stats["approved"]?.toDouble() ?: 0.0, Color(0xFF10B981)),
    DonutSegment("Pending",  stats["pending"]?.toDouble()  ?: 0.0, Color(0xFFF59E0B)),
    DonutSegment("Blocked",  stats["blocked"]?.toDouble()  ?: 0.0, Color(0xFFEF4444))
)

/**
 * Convenience: build donut segments from product stock stats.
 */
fun buildProductStatsSegments(stats: Map<String, Int>): List<DonutSegment> = listOf(
    DonutSegment("In Stock",    stats["inStock"]?.toDouble()    ?: 0.0, Color(0xFF10B981)),
    DonutSegment("Low Stock",   stats["lowStock"]?.toDouble()   ?: 0.0, Color(0xFFF59E0B)),
    DonutSegment("Out of Stock",stats["outOfStock"]?.toDouble() ?: 0.0, Color(0xFFEF4444))
)
