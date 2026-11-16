package com.saurabh.mediadminapp.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.dto.AuditLog
import com.saurabh.mediadminapp.data.v1.repository.AuditFilter
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.ChipLabel
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.GsFilterChip
import com.saurabh.mediadminapp.ui.components.InfoTile
import com.saurabh.mediadminapp.ui.components.PagedLazyColumn
import com.saurabh.mediadminapp.ui.components.SkeletonList
import com.saurabh.mediadminapp.ui.components.Tone
import com.saurabh.mediadminapp.ui.components.ToneChip
import com.saurabh.mediadminapp.ui.components.dateTimeLabel
import com.saurabh.mediadminapp.ui.components.shadeRow

/** Entity filter values (server `entity`); "All" = null. */
private val ENTITIES = listOf(null, "product", "batch", "order", "user", "admin", "supplier")

/** "supplier.deactivate" -> "Supplier deactivate". */
private fun actionLabel(action: String) =
    action.replace('.', ' ').replace('_', ' ').trim().replaceFirstChar { it.uppercase() }

/** Colour meaning by verb: delete/deactivate/block/reject/write-off = Red, create/approve = Green, else Teal (info). */
private fun actionTone(action: String): Tone {
    val a = action.lowercase()
    return when {
        listOf("delete", "deactivate", "block", "reject", "write", "cancel").any { it in a } -> Tone.Danger
        listOf("create", "approve", "register", "reactivate", "unblock").any { it in a } -> Tone.Success
        else -> Tone.Info
    }
}

/**
 * Audit trail (response.md #71), newest first. Entity chips; rows expand (no navigation) to show `details`
 * (sensitive keys are already stripped by the server). List row: shade only, no scale.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogScreen(viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.auditLogs.collectAsStateWithLifecycle()
    val filter by viewModel.auditFilter.collectAsStateWithLifecycle()
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.loadAuditLogs() }

    AppScreenScaffold(title = "Audit log", onBack = { navController.popBackStack() }) { inner ->
        PagedLazyColumn(
            isRefreshing = state.isLoading && state.loaded,
            onRefresh = { viewModel.loadAuditLogs(force = true) },
            isLoadingMore = state.isLoadingMore,
            endReached = state.endReached,
            onLoadMore = viewModel::loadMoreAuditLogs,
            state = rememberLazyListState(),
            modifier = Modifier.padding(inner).fillMaxSize(),
        ) {
            item(key = "chips", contentType = "chips") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ENTITIES, key = { it ?: "all" }) { e ->
                        GsFilterChip(
                            selected = filter.entity == e,
                            onClick = { viewModel.loadAuditLogs(AuditFilter(entity = e)) },
                            label = { ChipLabel(e?.replaceFirstChar { it.uppercase() } ?: "All") },
                        )
                    }
                }
            }
            state.total?.let { t ->
                item(key = "count", contentType = "caption") {
                    Text("$t entr${if (t == 1) "y" else "ies"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            when {
                !state.loaded && state.error != null -> item(key = "error", contentType = "state") { ErrorState(state.error ?: "Could not load", { viewModel.loadAuditLogs(force = true) }) }
                !state.loaded -> item(key = "skeleton", contentType = "state") { SkeletonList(rows = 6) }
                state.items.isEmpty() -> item(key = "empty", contentType = "state") { EmptyState("No audit entries", "Admin actions appear here.") }
                else -> items(state.items, key = { it.auditId }, contentType = { "audit" }) { log ->
                    AuditRow(log, expanded = expanded == log.auditId) { expanded = if (expanded == log.auditId) null else log.auditId }
                }
            }
        }
    }
}

@Composable
private fun AuditRow(log: AuditLog, expanded: Boolean, onToggle: () -> Unit) {
    val details = log.details?.entrySet()?.toList().orEmpty()
    Column(
        Modifier
            .fillMaxWidth()
            .shadeRow(onToggle)
            .animateContentSize(tween(200))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ToneChip(actionLabel(log.action), actionTone(log.action))
            ToneChip(log.entity.replaceFirstChar { it.uppercase() }, Tone.Info)
            Text(
                log.createdAt.dateTimeLabel(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
            )
            if (details.isNotEmpty()) Icon(
                if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = if (expanded) "Hide details" else "Show details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(log.entityId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            "by ${log.actorId} (${log.actorRole})", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
        if (expanded && details.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            details.forEach { (k, v) -> InfoTile(k.replace('_', ' ').replaceFirstChar { it.uppercase() }, v.display()) }
        }
    }
}

/** JSON value -> short text (strings unquoted, objects/arrays compact). */
private fun JsonElement.display(): String = when {
    isJsonNull -> "-"
    isJsonPrimitive -> asJsonPrimitive.let { if (it.isString) it.asString else it.toString() }
    this is JsonObject -> entrySet().joinToString(", ") { "${it.key}: ${it.value.display()}" }
    else -> toString()
}
