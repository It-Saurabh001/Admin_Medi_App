package com.saurabh.mediadminapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.graphics.graphicsLayer
import com.saurabh.mediadminapp.ui.theme.GsUi
import com.saurabh.mediadminapp.ui.theme.Shade
import com.saurabh.mediadminapp.ui.theme.isLight

/**
 * GreenShield OutlinedTextField (U9 / Part 3 §3): White fill, Outline 1dp unfocused, Green 500 2dp focused,
 * Red 600 error, label Muted -> Green 600. Same parameters as M3 OutlinedTextField(String).
 */
@Composable
fun GsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = MaterialTheme.shapes.small,
    colors: TextFieldColors = GsUi.fieldColors(),
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, modifier = modifier, enabled = enabled, readOnly = readOnly,
        textStyle = textStyle, label = label, placeholder = placeholder, leadingIcon = leadingIcon, trailingIcon = trailingIcon,
        prefix = prefix, suffix = suffix, supportingText = supportingText, isError = isError,
        visualTransformation = visualTransformation, keyboardOptions = keyboardOptions, keyboardActions = keyboardActions,
        singleLine = singleLine, maxLines = maxLines, minLines = minLines, interactionSource = interactionSource,
        shape = shape, colors = colors,
    )
}

/**
 * GreenShield FilterChip: unselected White + Outline (press Green 50 / hold Green 100),
 * selected Green 100 + Green 600 border/text (press 200 / hold 300). No ripple.
 */
@Composable
fun GsFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val source = remember { MutableInteractionSource() }
    val light = MaterialTheme.isLight
    val cs = MaterialTheme.colorScheme
    val shade = when {
        !light -> GsUi.rowShade()
        selected -> com.saurabh.mediadminapp.ui.theme.Shades.GreenChipSelected
        else -> GsUi.rowShade()
    }
    val bg by source.shadeColor(if (selected && !light) Shade(cs.secondaryContainer, cs.secondaryContainer, cs.secondaryContainer) else shade, "chip")
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        interactionSource = source,
        shape = MaterialTheme.shapes.small,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = bg,
            selectedContainerColor = bg,
            labelColor = cs.onSurface,
            selectedLabelColor = cs.onSecondaryContainer,
            iconColor = cs.onSurfaceVariant,
            selectedLeadingIconColor = cs.onSecondaryContainer,
            selectedTrailingIconColor = cs.onSecondaryContainer,
        ),
        border = if (selected) BorderStroke(1.dp, cs.primary) else BorderStroke(1.dp, cs.outline),
    )
}

/**
 * Text link ("View all", "Forgot password?", "Resend"): [shade] text 600 -> 700 (press) -> 800 (hold), no ripple,
 * 48dp min target. Use [com.saurabh.mediadminapp.ui.theme.Shades.RedText] for "Remove", AmberText for "Restock now".
 */
@Composable
fun LinkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shade: Shade = com.saurabh.mediadminapp.ui.theme.Shades.GreenText,
) {
    val source = remember { MutableInteractionSource() }
    val light = MaterialTheme.isLight
    val color by source.shadeColor(if (light) shade else Shade(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary), "link")
    androidx.compose.material3.TextButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = source,
        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = color),
        modifier = modifier.heightIn(min = 48.dp),
    ) { Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) }
}

/** Neutral dialog/dismiss text button ("Cancel"): Ink -> Green 700 (press) -> Green 800 (hold). */
@Composable
fun NeutralTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val ink = MaterialTheme.colorScheme.onSurface
    LinkButton(
        text, onClick, modifier, enabled,
        shade = Shade(ink, com.saurabh.mediadminapp.ui.theme.GS.Green700, com.saurabh.mediadminapp.ui.theme.GS.Green800),
    )
}

/**
 * The one FAB per screen (U9): solid Green 600 -> 700 (press) -> 800 (hold), White content, 0.97 scale, no ripple.
 * [expanded] collapses to the icon (e.g. while scrolling).
 */
@Composable
fun GsFab(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, expanded: Boolean = true) {
    val source = remember { MutableInteractionSource() }
    val bg by source.shadeColor(com.saurabh.mediadminapp.ui.theme.Shades.GreenSolid, "fab")
    val scale by source.pressScale()
    androidx.compose.material3.ExtendedFloatingActionButton(
        text = { Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1) },
        icon = { androidx.compose.material3.Icon(icon, contentDescription = null) },
        onClick = onClick,
        expanded = expanded,
        interactionSource = source,
        containerColor = bg,
        contentColor = com.saurabh.mediadminapp.ui.theme.GS.White,
        elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(2.dp, 2.dp, 2.dp, 2.dp),
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
    )
}

/** Chip label text: labelMedium, one line (U6). */
@Composable
fun ChipLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
}
