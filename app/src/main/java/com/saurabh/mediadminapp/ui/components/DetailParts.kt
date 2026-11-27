package com.saurabh.mediadminapp.ui.components

import com.saurabh.mediadminapp.ui.theme.isLight
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.saurabh.mediadminapp.ui.theme.GS
import com.saurabh.mediadminapp.ui.theme.GsUi
import androidx.compose.ui.semantics.contentDescription
import com.saurabh.mediadminapp.ui.theme.Shade
import com.saurabh.mediadminapp.ui.theme.Shades

/** White outlined data block (Outline border) with an optional section title. */
@Composable
fun DetailCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (title != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (icon != null) Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Text(
                        title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { heading() }
                    )
                }
            }
            content()
        }
    }
}

/** Field-fill info tile: Muted label over Ink bold value (Category, Pack size, IDs...). */
@Composable
fun InfoTile(label: String, value: String, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Surface(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            Modifier.heightIn(min = 56.dp).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (icon != null) Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface, maxLines = 3, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Label left, value right (bill lines). */
@Composable
fun KeyValueRow(label: String, value: String, modifier: Modifier = Modifier, emphasize: Boolean = false) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (emphasize) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            value,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}

enum class Tone { Success, Warning, Danger, Info }

/** Semantic pill for non-order states (approved, blocked, Rx required, role...). Same families as StatusChip. */
@Composable
fun ToneChip(text: String, tone: Tone, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val (bg, fg) = if (!MaterialTheme.isLight) when (tone) {
        Tone.Success -> cs.primaryContainer to cs.onPrimaryContainer
        Tone.Warning -> cs.tertiaryContainer to cs.onTertiaryContainer
        Tone.Danger -> cs.errorContainer to cs.onErrorContainer
        Tone.Info -> cs.secondaryContainer to cs.onSecondaryContainer
    } else when (tone) {
        Tone.Success -> GS.Green100 to GS.Green600
        Tone.Warning -> GS.Amber100 to GS.Amber600
        Tone.Danger -> GS.Red100 to GS.Red600
        Tone.Info -> GS.Teal100 to GS.Teal600
    }
    Surface(modifier = modifier.heightIn(min = 24.dp), shape = MaterialTheme.shapes.small, color = bg, contentColor = fg) {
        Text(
            text, style = MaterialTheme.typography.labelMedium,
            fontWeight = if (tone == Tone.Warning || tone == Tone.Danger) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/** Round person photo; initial on Green 100 / Green 600 when the url is missing or fails. */
@Composable
fun PersonAvatar(url: String?, name: String, modifier: Modifier = Modifier, size: Dp = 96.dp) {
    val initial = name.trim().firstOrNull()?.uppercase() ?: "?"
    val fallback = @Composable {
        val light = MaterialTheme.isLight
        val bg = if (light) GS.Green100 else MaterialTheme.colorScheme.primaryContainer
        val fg = if (light) GS.Green600 else MaterialTheme.colorScheme.onPrimaryContainer
        Box(Modifier.size(size).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
            Text(initial, style = MaterialTheme.typography.headlineMedium, color = fg)
        }
    }
    if (url.isNullOrBlank()) { Box(modifier) { fallback() }; return }
    SubcomposeAsyncImage(
        model = url, contentDescription = null, contentScale = ContentScale.Crop,
        modifier = modifier.size(size).clip(CircleShape),
        loading = { fallback() }, error = { fallback() }
    )
}

/** 1:1 medicine image, always ContentScale.Fit (never crop strips). Teal tile + Medication icon when missing. */
@Composable
fun MedicineImage(url: String?, modifier: Modifier = Modifier, size: Dp = 64.dp) {
    val shape = MaterialTheme.shapes.small
    val fallback = @Composable {
        val light = MaterialTheme.isLight
        val bg = if (light) GS.Teal100 else MaterialTheme.colorScheme.secondaryContainer
        val fg = if (light) GS.Teal600 else MaterialTheme.colorScheme.onSecondaryContainer
        Box(Modifier.size(size).clip(shape).background(bg), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Medication, contentDescription = null, tint = fg, modifier = Modifier.size(size / 2))
        }
    }
    if (url.isNullOrBlank()) { Box(modifier) { fallback() }; return }
    SubcomposeAsyncImage(
        model = url, contentDescription = null, contentScale = ContentScale.Fit,
        modifier = modifier.size(size).clip(shape).background(GS.White)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape).padding(4.dp),
        loading = { fallback() }, error = { fallback() }
    )
}

/** Solid button: normal -> pressed shade (same family), 0.97 scale, disabled = Green 300. */
@Composable
fun ShadeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shade: Shade = Shades.GreenSolid,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
) {
    val source = remember { MutableInteractionSource() }
    val scale by source.pressScale()
    val container by source.shadeColor(shade, "shadeBtnColor")
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        interactionSource = source,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = GS.White,
            disabledContainerColor = if (isLoading) shade.normal else GS.Green300,
            disabledContentColor = GS.White,
        ),
        modifier = modifier.heightIn(min = 48.dp).graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = GS.White)
        } else {
            if (icon != null) { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)) }
            Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Secondary: white + Outline border, press = Green 100. */
@Composable
fun ShadeOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    val source = remember { MutableInteractionSource() }
    val scale by source.pressScale()
    val container by source.shadeColor(GsUi.secondaryButtonShade(), "outBtnColor")
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = source,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = container, contentColor = contentColor),
        modifier = modifier.heightIn(min = 48.dp).graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        if (icon != null) { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Tappable list row: White -> Green 50 (press) -> Green 100 (hold). No ripple, no scale (list rows). */
@Composable
fun NavRow(title: String, onClick: () -> Unit, modifier: Modifier = Modifier, subtitle: String? = null, icon: ImageVector? = null) {
    val source = remember { MutableInteractionSource() }
    val bg by source.shadeColor(GsUi.rowShade(), "navRow")
    Row(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(bg)
            .clickable(interactionSource = source, indication = null, onClick = onClick)
            .heightIn(min = 56.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) {
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp)) }
            }
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Tappable list row/card surface: White -> Green 50 on press, Green 100 on long-press hold, 1dp Outline border,
 * no ripple, no scale (rule U13). Pair with a transparent ListItem/Column inside.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun Modifier.shadeRow(onClick: () -> Unit, onLongClick: (() -> Unit)? = null, shade: Shade? = null): Modifier {
    val source = remember { MutableInteractionSource() }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val shape = MaterialTheme.shapes.medium
    val bg by source.shadeColor(shade?.let { GsUi.familyShade(it) } ?: GsUi.rowShade(), "shadeRow")
    return this
        .clip(shape)
        .background(bg)
        .border(1.dp, MaterialTheme.colorScheme.outline, shape)
        .combinedClickable(
            interactionSource = source, indication = null,
            onClick = onClick,
            onLongClick = onLongClick?.let { cb ->
                { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); cb() }
            },
        )
}

/**
 * Standalone icon button in an Icon-fill tile (#F4F9F5 -> Green 100 -> Green 200), 48dp target, no ripple.
 * Used for top-bar back / scan / bell buttons (U9).
 */
@Composable
fun TileIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val source = remember { MutableInteractionSource() }
    val bg by source.shadeColor(GsUi.iconButtonShade(), "tileIcon")
    val scale by source.pressScale(enabled)
    Box(
        modifier
            .size(48.dp)
            .clickable(interactionSource = source, indication = null, enabled = enabled, role = androidx.compose.ui.semantics.Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(40.dp).graphicsLayer { scaleX = scale; scaleY = scale }.clip(MaterialTheme.shapes.small).background(bg),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp)) }
    }
}

/** Tap on empty space clears focus (hides keyboard). */
@Composable
fun Modifier.clearFocusOnTap(): Modifier {
    val focus = LocalFocusManager.current
    return pointerInput(Unit) { detectTapGestures(onTap = { focus.clearFocus() }) }
}
