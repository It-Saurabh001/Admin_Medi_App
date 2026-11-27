package com.saurabh.mediadminapp.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.saurabh.mediadminapp.ui.theme.ThemeMode

/** System / Light / Dark picker; the choice applies immediately. */
@Composable
fun ThemeDialog(current: ThemeMode, onSelect: (ThemeMode) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Theme", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(MaterialTheme.shapes.small)
                            .selectable(
                                selected = mode == current, role = Role.RadioButton,
                                interactionSource = remember { MutableInteractionSource() }, indication = null,
                                onClick = { onSelect(mode) },
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = mode == current, onClick = null, modifier = Modifier.padding(horizontal = 8.dp))
                        Text(mode.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = { LinkButton("Done", onDismiss) },
    )
}
