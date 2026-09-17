package com.saurabh.mediadminapp.ui.screens.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saurabh.mediadminapp.ui.theme.ClayBorder
import com.saurabh.mediadminapp.ui.theme.ClayBorderFocused
import com.saurabh.mediadminapp.ui.theme.ClayFieldBg
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextMuted
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary

// =============================================================================
// ClayTextField.kt
//
// CRITICAL FIX: All OutlinedTextField instances previously used default Material3
// colors which produced a light-gray text color in the focused state — washing
// out against the clay card background.
//
// This component guarantees:
//   - textColor     = ClayTextPrimary (#1E293B) in ALL states (focused, unfocused, error)
//   - cursorColor   = ClayPrimary
//   - labelColor    = ClayTextSecondary (unfocused) / ClayPrimary (focused)
//   - containerColor = ClayFieldBg (#F0EEFF)
//   - borderColor   = ClayBorder (unfocused) / ClayPrimary (focused)
// =============================================================================

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import com.saurabh.mediadminapp.ui.theme.ClayError

/**
 * Canonical claymorphism text field.
 * Drop-in replacement for every OutlinedTextField in the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClayTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorText: String = "",
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    readOnly: Boolean = false
) {
    val shape = RoundedCornerShape(16.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = if (label.isNotEmpty()) {
                { Text(label, fontSize = 14.sp) }
            } else null,
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(placeholder, color = ClayTextMuted, fontSize = 14.sp) }
            } else null,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            enabled = enabled,
            readOnly = readOnly,
            shape = shape,
            textStyle = TextStyle(
                color = ClayTextPrimary,   // CRITICAL: crisp #1E293B in ALL states
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            ),
            colors = OutlinedTextFieldDefaults.colors(
                // ── Text Colors ─────────────────────────────────────────────────
                focusedTextColor    = ClayTextPrimary,
                unfocusedTextColor  = ClayTextPrimary,
                disabledTextColor   = ClayTextMuted,
                errorTextColor      = ClayTextPrimary,
                // ── Cursor ──────────────────────────────────────────────────────
                cursorColor         = ClayPrimary,
                errorCursorColor    = ClayPrimary,
                // ── Container ───────────────────────────────────────────────────
                focusedContainerColor   = ClayFieldBg,
                unfocusedContainerColor = ClayFieldBg,
                disabledContainerColor  = ClayFieldBg.copy(alpha = 0.5f),
                errorContainerColor     = ClayFieldBg,
                // ── Border ──────────────────────────────────────────────────────
                focusedBorderColor   = ClayBorderFocused,
                unfocusedBorderColor = ClayBorder,
                disabledBorderColor  = ClayBorder.copy(alpha = 0.4f),
                errorBorderColor     = ClayError,
                // ── Label ───────────────────────────────────────────────────────
                focusedLabelColor   = ClayPrimary,
                unfocusedLabelColor = ClayTextSecondary,
                disabledLabelColor  = ClayTextMuted,
                errorLabelColor     = ClayError,
                // ── Placeholder ─────────────────────────────────────────────────
                focusedPlaceholderColor   = ClayTextMuted,
                unfocusedPlaceholderColor = ClayTextMuted,
                // ── Leading/Trailing ────────────────────────────────────────────
                focusedLeadingIconColor   = ClayPrimary,
                unfocusedLeadingIconColor = ClayTextSecondary,
                focusedTrailingIconColor  = ClayPrimary,
                unfocusedTrailingIconColor = ClayTextSecondary,
            )
        )
        if (isError && errorText.isNotEmpty()) {
            Text(
                text = errorText,
                color = ClayError,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Overload of ClayTextField taking ImageVector leading icon.
 * Requiring leadingIcon to be non-null prevents overload ambiguity when icons are omitted.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClayTextField(
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    trailingIcon: ImageVector? = null,
    isError: Boolean = false,
    errorText: String = "",
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    readOnly: Boolean = false
) {
    val leadingLambda: @Composable () -> Unit = {
        Icon(imageVector = leadingIcon, contentDescription = label, tint = ClayTextSecondary)
    }
    val trailingLambda: (@Composable () -> Unit)? = trailingIcon?.let { icon ->
        { Icon(imageVector = icon, contentDescription = null, tint = ClayTextSecondary) }
    }
    ClayTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingLambda,
        trailingIcon = trailingLambda,
        isError = isError,
        errorText = errorText,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        enabled = enabled,
        readOnly = readOnly
    )
}

/**
 * Search-style clay text field with no label, larger placeholder, and a border radius of 50dp (pill).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClaySearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Search...",
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val shape = RoundedCornerShape(50.dp)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = ClayTextMuted) },
        leadingIcon = leadingIcon,
        singleLine = true,
        shape = shape,
        keyboardOptions = keyboardOptions,
        textStyle = TextStyle(
            color = ClayTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor        = ClayTextPrimary,
            unfocusedTextColor      = ClayTextPrimary,
            cursorColor             = ClayPrimary,
            focusedContainerColor   = ClayFieldBg,
            unfocusedContainerColor = ClayFieldBg,
            focusedBorderColor      = ClayBorderFocused,
            unfocusedBorderColor    = ClayBorder,
            focusedLeadingIconColor = ClayPrimary,
            unfocusedLeadingIconColor = ClayTextSecondary,
            focusedPlaceholderColor   = ClayTextMuted,
            unfocusedPlaceholderColor = ClayTextMuted
        )
    )
}
