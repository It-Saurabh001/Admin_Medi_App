package com.saurabh.mediadminapp.ui.screens

import com.saurabh.mediadminapp.ui.components.GsTextField
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.saurabh.mediadminapp.ui.theme.GS
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.saurabh.mediadminapp.data.v1.error.AppError

/** Backend `fields` message for [key] (e.g. "email", "otp", "new_password"), if any. */
internal fun AppError?.fieldError(key: String): String? = (this as? AppError.Api)?.fields?.get(key)

/** Key used to hand a one-line message back to the Login screen via savedStateHandle. */
internal const val AUTH_MESSAGE_KEY = "auth_message"

/** Scrollable, centred auth form body with 16dp margins. */
@Composable
internal fun AuthFormColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    content: @Composable ColumnScope.() -> Unit
) {
    val focus = androidx.compose.ui.platform.LocalFocusManager.current
    BoxWithConstraints(
        modifier = modifier.fillMaxSize().imePadding()
            // Tap on empty area dismisses keyboard (Part 3 §3); no indication.
            .pointerInput(Unit) { detectTapGestures(onTap = { focus.clearFocus() }) }
    ) {
        val minHeight = maxHeight
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = minHeight)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = verticalArrangement,
            content = content
        )
    }
}

/** Error-aware supporting text: shows [error] when present, else [hint]. */
@Composable
internal fun SupportingText(error: String?, hint: String? = null) {
    val text = error ?: hint ?: return
    Text(text = text, maxLines = 2, overflow = TextOverflow.Ellipsis)
}

@Composable
internal fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    modifier: Modifier = Modifier,
    hint: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    onDone: (() -> Unit)? = null,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    GsTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (visible) "Hide password" else "Show password"
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() ?: defaultKeyboardAction(ImeAction.Done) }),
        singleLine = true,
        isError = error != null,
        supportingText = if (error != null || hint != null) {
            { SupportingText(error, hint) }
        } else null,
        modifier = modifier.fillMaxWidth()
    )
}

/** Auth detail top bar (U9): White bar, Ink titleLarge, back arrow in an Icon-fill tile, Outline divider. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun AuthTopBar(title: String, onBack: () -> Unit) {
    Column {
        androidx.compose.material3.TopAppBar(
            title = { Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            navigationIcon = {
                com.saurabh.mediadminapp.ui.components.TileIconButton(
                    Icons.AutoMirrored.Outlined.ArrowBack, "Back", onBack, Modifier.padding(start = 4.dp)
                )
            },
            colors = com.saurabh.mediadminapp.ui.theme.GsUi.topBarColors(),
        )
        androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

/** Tonal icon badge + title + subtitle used at the top of auth forms. */
@Composable
internal fun AuthHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** "Prompt  Action" footer row, e.g. "Don't have an account? Create account". */
@Composable
internal fun AuthFooterLink(
    prompt: String,
    action: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        com.saurabh.mediadminapp.ui.components.LinkButton(text = action, onClick = onClick)
    }
}

/** Form-level error shown under the fields. */
@Composable
internal fun AuthFormError(text: String?) {
    text ?: return
    Spacer(modifier = Modifier.height(8.dp))
    Surface(
        shape = MaterialTheme.shapes.small,
        // Guide §8: error message in Red 600 on the Red 50/100 error card.
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.error,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

/** Full-width primary action with inline progress. */
@Composable
internal fun AuthPrimaryButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Press = Green 600 -> 700, hold = 800; 0.97 scale; loading keeps 600 + white 18dp spinner (rules U13, guide §8).
    com.saurabh.mediadminapp.ui.components.ShadeButton(
        text = text, onClick = onClick, isLoading = isLoading, modifier = modifier.fillMaxWidth(),
    )
}
