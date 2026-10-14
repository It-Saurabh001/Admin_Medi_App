package com.saurabh.mediadminapp.ui.auth.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OtpInput(
    code: String,
    onCodeChange: (String) -> Unit,
    onComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    length: Int = 6,
) {
    val focusRequesters = remember { List(length) { FocusRequester() } }

    // One hidden BasicTextField per digit
    val digits = code.padEnd(length).take(length)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        repeat(length) { index ->
            val isFocusedIndex = digits.length == index ||
                    (digits.length >= length && index == length - 1)

            val borderColor by animateColorAsState(
                targetValue = when {
                    isError -> MaterialTheme.colorScheme.error
                    digits.length > index -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                },
                animationSpec = tween(200),
                label = "otp_border_$index"
            )

            var fieldValue by remember { mutableStateOf(TextFieldValue("")) }

            LaunchedEffect(code) {
                val ch = if (index < code.length) code[index].toString() else ""
                fieldValue = TextFieldValue(text = ch, selection = TextRange(ch.length))
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(42.dp)
                    .height(50.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = if (digits.length > index) 2.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .semantics { contentDescription = "OTP digit ${index + 1}" }
            ) {
                BasicTextField(
                    value = fieldValue,
                    onValueChange = { newValue ->
                        val newText = newValue.text.filter { it.isDigit() }
                        if (newText.isEmpty()) {
                            // Backspace — remove this digit
                            val newCode = code.dropLast(if (code.length > index) code.length - index else 0)
                                .take(index)
                            onCodeChange(newCode)
                            if (index > 0) {
                                try { focusRequesters[index - 1].requestFocus() } catch (_: Exception) {}
                            }
                        } else {
                            // Paste of full 6-digit code
                            if (newText.length == length) {
                                onCodeChange(newText)
                                onComplete(newText)
                                try { focusRequesters[length - 1].requestFocus() } catch (_: Exception) {}
                                return@BasicTextField
                            }
                            val char = newText.last()
                            val newCode = (code.take(index) + char).take(length)
                            onCodeChange(newCode)
                            if (newCode.length == length) onComplete(newCode)
                            if (index < length - 1) {
                                try { focusRequesters[index + 1].requestFocus() } catch (_: Exception) {}
                            }
                        }
                    },
                    modifier = Modifier
                        .width(42.dp)
                        .height(50.dp)
                        .focusRequester(focusRequesters[index])
                        .onKeyEvent { event ->
                            if (event.key == Key.Backspace && fieldValue.text.isEmpty() && index > 0) {
                                val newCode = code.dropLast(code.length - index)
                                onCodeChange(newCode)
                                try { focusRequesters[index - 1].requestFocus() } catch (_: Exception) {}
                                true
                            } else false
                        },
                    enabled = enabled,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = if (index < length - 1) ImeAction.Next else ImeAction.Done
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.Center) {
                            if (fieldValue.text.isEmpty()) {
                                Text(
                                    text = "–",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                            inner()
                        }
                    }
                )
            }
        }
    }
}
