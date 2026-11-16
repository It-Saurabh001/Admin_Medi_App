package com.saurabh.mediadminapp.ui.screens

import com.saurabh.mediadminapp.ui.components.GsFilterChip
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.core.UiState
import com.saurabh.mediadminapp.data.v1.dto.Batch
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.shadeRow
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.PagedLazyColumn
import com.saurabh.mediadminapp.ui.components.SkeletonList
import com.saurabh.mediadminapp.ui.components.showDate
import com.saurabh.mediadminapp.ui.screens.nav.Routes

/** Batches of one product. Expired in red, near expiry highlighted; tap opens BatchDetail. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchListScreen(productId: String, viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.productBatches.collectAsStateWithLifecycle()
    val product by viewModel.getSpecificProductState.collectAsStateWithLifecycle()
    LaunchedEffect(productId) { viewModel.loadProductBatches(productId, force = true) }
    var hideExpired by rememberSaveable { mutableStateOf(false) }
    var inStockOnly by rememberSaveable { mutableStateOf(false) }

    val all = (state as? UiState.Success)?.data.orEmpty()
    val shown = remember(all, hideExpired, inStockOnly) {
        all.filter { (!hideExpired || !it.isExpired) && (!inStockOnly || it.quantity > 0) }
    }
    val title = all.firstOrNull()?.productName ?: product.success?.takeIf { it.productId == productId }?.name ?: "Batches"

    AppScreenScaffold(
        title = title,
        onBack = { navController.navigateUp() },
        actions = {
            com.saurabh.mediadminapp.ui.components.TileIconButton(
                Icons.Outlined.Tune, "Adjust stock", { navController.navigate(Routes.StockAdjustRoutes.invoke(productId)) { launchSingleTop = true } }
            )
        },
        floatingActionButton = {
            com.saurabh.mediadminapp.ui.components.GsFab(
                text = "Receive stock", icon = Icons.Outlined.Add,
                onClick = { navController.navigate(Routes.StockPurchaseRoutes.invoke(productId)) { launchSingleTop = true } },
            )
        }
    ) { inner ->
        PagedLazyColumn(
            isRefreshing = state is UiState.Loading && all.isNotEmpty(),
            onRefresh = { viewModel.loadProductBatches(productId, force = true) },
            isLoadingMore = false, endReached = true, onLoadMore = {},
            state = rememberLazyListState(),
            modifier = Modifier.padding(inner).fillMaxSize()
        ) {
            when (val s = state) {
                is UiState.Error -> item(key = "error", contentType = "state") {
                    ErrorState(s.error.userMessage(), onRetry = { viewModel.loadProductBatches(productId, force = true) })
                }
                is UiState.Empty -> item(key = "empty", contentType = "state") {
                    EmptyState(title = "No batches yet", message = "Receive stock to create the first batch.")
                }
                is UiState.Success -> {
                    item(key = "summary", contentType = "summary") {
                        Text(
                            "${all.sumOf { it.quantity }} units in ${all.size} batch${if (all.size == 1) "" else "es"}",
                            style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item(key = "filters", contentType = "filters") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("Hide expired", "In stock only"), key = { it }) { f ->
                                val on = if (f == "Hide expired") hideExpired else inStockOnly
                                GsFilterChip(selected = on, onClick = { if (f == "Hide expired") hideExpired = !on else inStockOnly = !on }, label = { Text(f) })
                            }
                        }
                    }
                    if (shown.isEmpty()) item(key = "none", contentType = "state") { EmptyState("No matching batches", "Change the filters above.") }
                    items(shown, key = { it.batchId }, contentType = { "batch" }) { b ->
                        BatchRow(b) { navController.navigate(Routes.BatchDetailRoutes.invoke(b.batchId)) { launchSingleTop = true } }
                    }
                }
                else -> item(key = "skeleton", contentType = "state") { SkeletonList(rows = 4) }
            }
        }
    }
}

@Composable
internal fun BatchRow(b: Batch, onClick: () -> Unit) {
    val near = !b.isExpired && (b.daysToExpiry ?: Int.MAX_VALUE) in 0..90
    val shade = when {
        b.isExpired -> com.saurabh.mediadminapp.ui.theme.Shades.RedCard
        near -> com.saurabh.mediadminapp.ui.theme.Shades.AmberCard
        else -> com.saurabh.mediadminapp.ui.theme.Shades.GreenRow
    }
    ListItem(
        // U14: batch id in a Teal (info) pill; expiry state in its warning / danger pill.
        overlineContent = { com.saurabh.mediadminapp.ui.components.ToneChip("Batch ${b.batchNo}", com.saurabh.mediadminapp.ui.components.Tone.Info) },
        headlineContent = {
            when {
                b.isExpired -> com.saurabh.mediadminapp.ui.components.ToneChip("Expired", com.saurabh.mediadminapp.ui.components.Tone.Danger)
                near -> com.saurabh.mediadminapp.ui.components.ToneChip("Near expiry", com.saurabh.mediadminapp.ui.components.Tone.Warning)
                b.quantity <= 0 -> com.saurabh.mediadminapp.ui.components.ToneChip("Empty", com.saurabh.mediadminapp.ui.components.Tone.Danger)
                else -> com.saurabh.mediadminapp.ui.components.ToneChip("Available", com.saurabh.mediadminapp.ui.components.Tone.Success)
            }
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    when {
                        b.isExpired -> "Expired ${b.expiryDate.showDate()}"
                        b.daysToExpiry != null -> "Expires ${b.expiryDate.showDate()} (${b.daysToExpiry} days)"
                        else -> "Expires ${b.expiryDate.showDate()}"
                    },
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    "₹%.2f buy · ₹%.2f MRP".format(b.purchasePrice, b.mrp) + if (b.supplierName.isNotBlank()) " · ${b.supplierName}" else "",
                    style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                if (b.isLegacyPlaceholder) Text("Legacy placeholder: set the real expiry", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        },
        trailingContent = {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text("${b.quantity}", style = MaterialTheme.typography.titleLarge, maxLines = 1)
                Text("units", style = MaterialTheme.typography.labelSmall)
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            headlineColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.fillMaxWidth().shadeRow(onClick, shade = shade)
    )
}
