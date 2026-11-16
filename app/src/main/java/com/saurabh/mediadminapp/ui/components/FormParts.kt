package com.saurabh.mediadminapp.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SHOW_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

fun LocalDate?.showDate(): String = this?.format(SHOW_FMT) ?: "-"

/** Read-only date field with a Material date picker. [iso] is yyyy-MM-dd or empty. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    label: String,
    iso: String,
    onIsoChange: (String) -> Unit,
    enabled: Boolean,
    error: String?,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    var show by rememberSaveable { mutableStateOf(false) }
    val date = runCatching { LocalDate.parse(iso) }.getOrNull()
    GsTextField(
        value = date?.format(SHOW_FMT).orEmpty(), onValueChange = {}, readOnly = true,
        label = { Text(label, maxLines = 1) }, singleLine = true, enabled = enabled,
        trailingIcon = { IconButton(onClick = { show = true }, enabled = enabled) { Icon(Icons.Outlined.CalendarMonth, contentDescription = "Pick $label") } },
        isError = error != null, supportingText = error?.let { { Text(it) } },
        modifier = modifier
    )
    if (show) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = date?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(enabled = picker.selectedDateMillis != null, onClick = {
                    picker.selectedDateMillis?.let { onIsoChange(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString()) }
                    show = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text("Cancel") } }
        ) { DatePicker(state = picker) }
    }
}

/** Bottom bar with one primary action; shows a spinner while [loading]. */
@Composable
fun SaveBar(text: String, loading: Boolean, onClick: () -> Unit, enabled: Boolean = true) {
    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        ShadeButton(
            text = text, onClick = onClick, enabled = enabled, isLoading = loading,
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}
