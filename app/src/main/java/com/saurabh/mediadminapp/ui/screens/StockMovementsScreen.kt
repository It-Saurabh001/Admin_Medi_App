package com.saurabh.mediadminapp.ui.screens

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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.dto.StockMovement
import com.saurabh.mediadminapp.data.v1.repository.StockMovementFilter
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.ChipLabel
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.GsFilterChip
import com.saurabh.mediadminapp.ui.components.PagedLazyColumn
import com.saurabh.mediadminapp.ui.components.SkeletonList
import com.saurabh.mediadminapp.ui.components.Tone
import com.saurabh.mediadminapp.ui.components.ToneChip
import com.saurabh.mediadminapp.ui.components.dateTimeLabel
import com.saurabh.mediadminapp.ui.components.shadeRow
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.ui.theme.GS
import com.saurabh.mediadminapp.ui.theme.isLight
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord

/** Wire reasons (response.md enums) -> label + colour family (Part 5: Green = in, Red = loss, Teal = info). */
private val REASONS = listOf(
    "PURCHASE" to "Purchase",
    "SALE" to "Sale",
    "ADJUSTMENT" to "Adjustment",
    "RETURN" to "Return",
    "CANCEL_RESTOCK" to "Cancel restock",
    "EXPIRED_WRITEOFF" to "Write-off",
)

private fun reasonLabel(wire: String) = REASONS.firstOrNull { it.first == wire }?.second ?: wire.lowercase().replaceFirstChar { it.uppercase() }

private fun reasonTone(wire: String) = when (wire) {
    "PURCHASE", "RETURN", "CANCEL_RESTOCK" -> Tone.Success
    "EXPIRED_WRITEOFF" -> Tone.Danger
    "SALE" -> Tone.Info
    else -> Tone.Warning // ADJUSTMENT
}

/**
 * Stock movement ledger (response.md #44), newest first. Reason chips; optional product / batch scope from the
 * caller (Product details, Batch details) shown as a removable chip. Rows -> Batch details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockMovementsScreen(productId: String?, batchId: String?, viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.stockMovements.collectAsStateWithLifecycle()
    val filter by viewModel.stockMovementFilter.collectAsStateWithLifecycle()

    // Scope from the route wins on first open; reason is kept while browsing.
    LaunchedEffect(productId, batchId) {
        viewModel.loadStockMovements(StockMovementFilter(productId = productId, batchId = batchId))
    }
    val apply: (StockMovementFilter) -> Unit = { viewModel.loadStockMovements(it) }
    val scopeName = state.items.firstOrNull()?.let { m ->
        when {
            filter.batchId != null -> "Batch ${m.batchNo ?: filter.batchId}"
            filter.productId != null -> m.productName?.capitalizeEachWord() ?: "This product"
            else -> null
        }
    } ?: when {
        filter.batchId != null -> "This batch"
        filter.productId != null -> "This product"
        else -> null
    }

    AppScreenScaffold(title = "Stock movements", onBack = { navController.popBackStack() }) { inner ->
        PagedLazyColumn(
            isRefreshing = state.isLoading && state.loaded,
            onRefresh = { viewModel.loadStockMovements(force = true) },
            isLoadingMore = state.isLoadingMore,
            endReached = state.endReached,
            onLoadMore = viewModel::loadMoreStockMovements,
            state = rememberLazyListState(),
            modifier = Modifier.padding(inner).fillMaxSize(),
        ) {
            item(key = "chips", contentType = "chips") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (scopeName != null) item(key = "scope") {
                        GsFilterChip(
                            selected = true,
                            onClick = { apply(filter.copy(productId = null, batchId = null)) },
                            label = { ChipLabel(scopeName) },
                            trailingIcon = { Icon(Icons.Outlined.Close, contentDescription = "Show all products") },
                        )
                    }
                    item(key = "all") { GsFilterChip(selected = filter.reason == null, onClick = { apply(filter.copy(reason = null)) }, label = { ChipLabel("All") }) }
                    items(REASONS, key = { it.first }) { (wire, label) ->
                        GsFilterChip(selected = filter.reason == wire, onClick = { apply(filter.copy(reason = wire)) }, label = { ChipLabel(label) })
                    }
                }
            }
            state.total?.let { t ->
                item(key = "count", contentType = "caption") {
                    Text("$t movement${if (t == 1) "" else "s"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            when {
                !state.loaded && state.error != null -> item(key = "error", contentType = "state") {
                    ErrorState(state.error ?: "Could not load", { viewModel.loadStockMovements(force = true) })
                }
                !state.loaded -> item(key = "skeleton", contentType = "state") { SkeletonList(rows = 6) }
                state.items.isEmpty() -> item(key = "empty", contentType = "state") {
                    EmptyState("No stock movements", "Purchases, sales, adjustments and write-offs appear here.")
                }
                else -> items(state.items, key = { it.movementId }, contentType = { "movement" }) { m ->
                    MovementRow(m) { navController.navigate(Routes.BatchDetailRoutes.invoke(m.batchId)) { launchSingleTop = true } }
                }
            }
        }
    }
}

/** Reason pill (family), product + Teal batch pill, signed quantity (Green in / Red out), time, actor, reference. */
@Composable
private fun MovementRow(m: StockMovement, onClick: () -> Unit) {
    val light = MaterialTheme.isLight
    val positive = m.changeQty > 0
    ListItem(
        overlineContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                ToneChip(reasonLabel(m.reason), reasonTone(m.reason))
                m.batchNo?.let { ToneChip("Batch $it", Tone.Info) }
            }
        },
        headlineContent = {
            Text(
                m.productName?.capitalizeEachWord() ?: m.productId, style = MaterialTheme.typography.titleMedium,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("${m.createdAt.dateTimeLabel()} · by ${m.performedBy}", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                m.referenceId?.takeIf { it.isNotBlank() }?.let {
                    Text("Ref $it", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (positive) "+" else "") + m.changeQty, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                    color = when { !light -> MaterialTheme.colorScheme.onSurface; positive -> GS.Green600; else -> GS.Red600 }, maxLines = 1,
                )
                m.productStock?.let { Text("Stock $it", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth().shadeRow(onClick),
    )
}
