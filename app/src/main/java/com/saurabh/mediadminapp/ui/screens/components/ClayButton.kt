package com.saurabh.mediadminapp.ui.screens.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saurabh.mediadminapp.ui.theme.ClayBorder
import com.saurabh.mediadminapp.ui.theme.ClayButtonGradient
import com.saurabh.mediadminapp.ui.theme.ClayCardBg
import com.saurabh.mediadminapp.ui.theme.ClayElevationButton
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayRadiusMedium
import com.saurabh.mediadminapp.ui.theme.ClayRadiusPill
import com.saurabh.mediadminapp.ui.theme.ClayTextOnDark
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary

// =============================================================================
// ClayButton.kt — Gradient primary button, outlined variant, and filter chips
// =============================================================================

/**
 * Primary gradient pill button with spring press animation and loading indicator.
 */
@Composable
fun ClayPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    gradient: Brush = ClayButtonGradient,
    cornerRadius: Dp = ClayRadiusPill,
    elevation: Dp = ClayElevationButton
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "btnPress"
    )
    val alpha = if (enabled) 1f else 0.6f
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
            .shadow(
                elevation = if (pressed) 4.dp else elevation,
                shape = shape,
                ambientColor = ClayPrimary.copy(0.3f),
                spotColor = ClayPrimary.copy(0.3f)
            )
            .clip(shape)
            .background(brush = gradient)
            .pointerInput(enabled, onClick) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { if (!isLoading) onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150))
        ) {
            CircularProgressIndicator(
                color = ClayTextOnDark,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(24.dp)
            )
        }
        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150))
        ) {
            Text(
                text = text,
                color = ClayTextOnDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Outlined clay button — bordered with ClayPrimary color, transparent background.
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
            .shadow(6.dp, shape, ambientColor = accentColor.copy(0.1f), spotColor = accentColor.copy(0.1f))
            .clip(shape)
            .background(ClayCardBg)
            .border(1.5.dp, accentColor.copy(if (enabled) 1f else 0.4f), shape)
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
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = text,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
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
 * Filter chip used in Home/Product/Order filter rows.
 * Selected state: gradient background + white text.
 * Unselected state: clay card background + primary-tinted text.
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
    val shape = RoundedCornerShape(ClayRadiusPill)
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.7f,
        label = "chipAlpha"
    )
    Box(
        modifier = modifier
            .wrapContentSize()
            .shadow(
                elevation = if (selected) 8.dp else 2.dp,
                shape = shape,
                ambientColor = ClayPrimary.copy(0.15f),
                spotColor = ClayPrimary.copy(0.15f)
            )
            .clip(shape)
            .background(if (selected) Color.Transparent else ClayCardBg)
            .then(
                if (selected) Modifier.background(brush = gradient, shape = shape)
                else Modifier.border(1.dp, ClayBorder, shape)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) ClayTextOnDark else ClayTextSecondary,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { this.alpha = alpha }
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                color = if (selected) ClayTextOnDark else ClayTextSecondary,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.graphicsLayer { this.alpha = alpha }
            )
        }
    }
}

/**
 * Danger button (delete actions) with a red-tinted gradient.
 */
@Composable
fun ClayDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    ClayPrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        isLoading = isLoading,
        enabled = enabled,
        gradient = Brush.horizontalGradient(
            colors = listOf(Color(0xFFEF4444), Color(0xFFFF6584))
        )
    )
}
