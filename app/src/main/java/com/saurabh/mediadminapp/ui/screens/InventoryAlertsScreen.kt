package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.InventoryAlertType
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.PagedListState
import com.saurabh.mediadminapp.data.v1.dto.Batch
import com.saurabh.mediadminapp.data.v1.dto.Product
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.ChipLabel
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.GsFilterChip
import com.saurabh.mediadminapp.ui.components.MedicineImage
import com.saurabh.mediadminapp.ui.components.PagedLazyColumn
import com.saurabh.mediadminapp.ui.components.SkeletonList
import com.saurabh.mediadminapp.ui.components.Tone
import com.saurabh.mediadminapp.ui.components.ToneChip
import com.saurabh.mediadminapp.ui.components.shadeRow
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.ui.theme.GS
import com.saurabh.mediadminapp.ui.theme.isLight
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord

private val NEAR_EXPIRY_DAYS = listOf(30, 60, 90)

/**
 * Inventory alerts (response.md #45-48): status tabs (PrimaryTabRow, U9) Low stock / Out of stock / Near expiry / Expired.
 * Product rows -> Product details; batch rows (shared BatchRow) -> Batch details. PTR + load-more per tab.
 * [initialTab] = InventoryAlertType.name (Dashboard tiles deep-link here).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryAlertsScreen(initialTab: String?, viewModel: MyViewModel, navController: NavController) {
    val tabs = InventoryAlertType.entries
    var tab by rememberSaveable { mutableIntStateOf(tabs.indexOfFirst { it.name == initialTab }.coerceAtLeast(0)) }
    var nearDays by rememberSaveable { mutableStateOf(90) }
    val type = tabs[tab]

    val low by viewModel.lowStock.collectAsStateWithLifecycle()
    val out by viewModel.outOfStock.collectAsStateWithLifecycle()
    val near by viewModel.nearExpiry.collectAsStateWithLifecycle()
    val expired by viewModel.expiredBatches.collectAsStateWithLifecycle()

    LaunchedEffect(type, nearDays) { viewModel.loadInventoryAlert(type, nearExpiryDays = nearDays.takeIf { type == InventoryAlertType.NEAR_EXPIRY }) }
    val refresh = { viewModel.loadInventoryAlert(type, force = true, nearExpiryDays = nearDays.takeIf { type == InventoryAlertType.NEAR_EXPIRY }) }

    val openProduct: (String) -> Unit = { id -> navController.navigate(Routes.SpecificProductRoutes.invoke(id)) { launchSingleTop = true } }
    val openBatch: (String) -> Unit = { id -> navController.navigate(Routes.BatchDetailRoutes.invoke(id)) { launchSingleTop = true } }

    AppScreenScaffold(title = "Inventory alerts", onBack = { navController.popBackStack() }) { inner ->
        Column(Modifier.padding(inner).fillMaxSize()) {
            PrimaryTabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEachIndexed { i, t ->
                    val count = when (t) {
                        InventoryAlertType.LOW_STOCK -> low.total
                        InventoryAlertType.OUT_OF_STOCK -> out.total
                        InventoryAlertType.NEAR_EXPIRY -> near.total
                        InventoryAlertType.EXPIRED -> expired.total
                    }
                    Tab(
                        selected = tab == i, onClick = { tab = i },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                t.label + (count?.let { " ($it)" } ?: ""), style = MaterialTheme.typography.labelLarge,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            }
            when (type) {
                InventoryAlertType.LOW_STOCK -> AlertList(low, refresh, { viewModel.loadMoreInventoryAlert(type) },
                    emptyTitle = "No low stock", emptyMessage = "Every active product is above its reorder level.") {
                    items(low.items, key = { it.productId }, contentType = { "product" }) { p -> AlertProductRow(p, out = false) { openProduct(p.productId) } }
                }
                InventoryAlertType.OUT_OF_STOCK -> AlertList(out, refresh, { viewModel.loadMoreInventoryAlert(type) },
                    emptyTitle = "Nothing out of stock", emptyMessage = "All active products have stock.") {
                    items(out.items, key = { it.productId }, contentType = { "product" }) { p -> AlertProductRow(p, out = true) { openProduct(p.productId) } }
                }
                InventoryAlertType.NEAR_EXPIRY -> AlertList(near, refresh, { viewModel.loadMoreInventoryAlert(type) },
                    emptyTitle = "No batches expiring soon", emptyMessage = "No in-stock batch expires in the next $nearDays days.",
                    header = {
                        item(key = "days", contentType = "chips") {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(NEAR_EXPIRY_DAYS, key = { it }) { d ->
                                    GsFilterChip(selected = nearDays == d, onClick = { nearDays = d }, label = { ChipLabel("$d days") })
                                }
                            }
                        }
                    }) {
                    items(near.items, key = { it.batchId }, contentType = { "batch" }) { b -> AlertBatchRow(b) { openBatch(b.batchId) } }
                }
                InventoryAlertType.EXPIRED -> AlertList(expired, refresh, { viewModel.loadMoreInventoryAlert(type) },
                    emptyTitle = "No expired stock", emptyMessage = "No in-stock batch is past its expiry date.") {
                    items(expired.items, key = { it.batchId }, contentType = { "batch" }) { b -> AlertBatchRow(b) { openBatch(b.batchId) } }
                }
            }
        }
    }
}

/** Loading / error+Retry / empty / rows inside the shared PagedLazyColumn (PTR + load-more). */
@Composable
private fun <T> AlertList(
    state: PagedListState<T>,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    emptyTitle: String,
    emptyMessage: String,
    header: LazyListScope.() -> Unit = {},
    rows: LazyListScope.() -> Unit,
) {
    PagedLazyColumn(
        isRefreshing = state.isLoading && state.loaded,
        onRefresh = onRefresh,
        isLoadingMore = state.isLoadingMore,
        endReached = state.endReached,
        onLoadMore = onLoadMore,
        state = rememberLazyListState(),
    ) {
        header()
        when {
            !state.loaded && state.error != null -> item(key = "error", contentType = "state") { ErrorState(state.error, onRefresh) }
            !state.loaded -> item(key = "skeleton", contentType = "state") { SkeletonList(rows = 5) }
            state.items.isEmpty() -> item(key = "empty", contentType = "state") { EmptyState(emptyTitle, emptyMessage) }
            else -> rows()
        }
    }
}

/** Product alert row: 1:1 Fit image, name, stock vs reorder level, Amber (low) / Red (out) pill. List row: shade only. */
@Composable
private fun AlertProductRow(p: Product, out: Boolean, onClick: () -> Unit) {
    val light = MaterialTheme.isLight
    ListItem(
        leadingContent = { MedicineImage(p.imageUrl, size = 56.dp) },
        headlineContent = { Text(p.name.capitalizeEachWord(), style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    listOf(p.strength, p.packSize).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { p.category.capitalizeEachWord() },
                    style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (out) ToneChip("Out of stock", Tone.Danger) else ToneChip("Low stock", Tone.Warning)
                    Text("Reorder at ${p.reorderLevel}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${p.stock}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1,
                    color = when { !light -> MaterialTheme.colorScheme.onSurface; out -> GS.Red600; else -> GS.Amber600 },
                )
                Text(p.unit.ifBlank { "units" }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth().shadeRow(onClick),
    )
}

/** Batch alert row: product name above the shared batch row (Teal batch pill, Expired / Near expiry pill). */
@Composable
private fun AlertBatchRow(b: Batch, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            b.productName.capitalizeEachWord(), style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        )
        BatchRow(b, onClick)
    }
}
