package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ReportQuery
import com.saurabh.mediadminapp.TopProductsState
import com.saurabh.mediadminapp.data.v1.dto.TopProduct
import com.saurabh.mediadminapp.ui.adaptive.rememberWindowSize
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.shadeRow
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.PagedLazyColumn
import com.saurabh.mediadminapp.ui.components.SkeletonList
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import java.text.NumberFormat

private val TOP_SORTS = listOf("quantity" to "Units sold", "revenue" to "Revenue")

/** Best sellers for a date range, sorted by units or revenue; tap opens ProductDetail. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopProductsScreen(viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.topProductsState.collectAsStateWithLifecycle()
    val window = rememberWindowSize()
    LaunchedEffect(Unit) { viewModel.loadTopProducts() }
    val onQuery: (ReportQuery) -> Unit = { viewModel.loadTopProducts(it) }
    val retry = { viewModel.loadTopProducts(force = true) }
    val open: (String) -> Unit = { id -> navController.navigate(Routes.SpecificProductRoutes.invoke(id)) { launchSingleTop = true } }

    AppScreenScaffold(title = "Top products", onBack = { navController.navigateUp() }) { inner ->
        if (window.isExpanded) {
            Row(Modifier.padding(inner).fillMaxSize()) {
                Column(Modifier.width(360.dp).fillMaxHeight().verticalScroll(rememberScrollState()).padding(16.dp)) {
                    ReportFilters(state.query, onQuery, showGroupBy = false, sortOptions = TOP_SORTS)
                }
                VerticalDivider()
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    TopProductRows(state, retry, viewModel::loadMoreTopProducts, open, header = null)
                }
            }
        } else {
            Column(Modifier.padding(inner).fillMaxSize()) {
                TopProductRows(state, retry, viewModel::loadMoreTopProducts, open) {
                    ReportFilters(state.query, onQuery, showGroupBy = false, sortOptions = TOP_SORTS)
                }
            }
        }
    }
}

@Composable
private fun TopProductRows(
    state: TopProductsState,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onOpen: (String) -> Unit,
    header: (@Composable () -> Unit)?,
) {
    val money = rememberInrFormat()
    PagedLazyColumn(
        isRefreshing = state.isLoading && state.loaded,
        onRefresh = onRetry,
        isLoadingMore = state.isLoadingMore,
        endReached = state.endReached,
        onLoadMore = onLoadMore,
        state = rememberLazyListState(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
    ) {
        if (header != null) item(key = "header", contentType = "header") {
            Column { header(); Spacer(Modifier.height(8.dp)) }
        }
        when {
            state.isLoading && !state.loaded -> item(key = "skeleton", contentType = "skeleton") { SkeletonList(rows = 6) }
            state.error != null && state.rows.isEmpty() -> item(key = "error", contentType = "state") {
                ErrorState(message = state.error, onRetry = onRetry)
            }
            state.loaded && state.rows.isEmpty() -> item(key = "empty", contentType = "state") {
                EmptyState(title = "No sales in this range", message = "Try a wider date range.")
            }
            else -> itemsIndexed(
                state.rows,
                key = { i, p -> p.productId ?: "row_$i" },
                contentType = { _, _ -> "product" }
            ) { i, p -> TopProductItem(rank = i + 1, p = p, money = money, onOpen = onOpen) }
        }
    }
}

@Composable
private fun TopProductItem(rank: Int, p: TopProduct, money: NumberFormat, onOpen: (String) -> Unit) {
    val id = p.productId
    ListItem(
        leadingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "#$rank",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (rank <= 3) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp),
                    maxLines = 1
                )
                com.saurabh.mediadminapp.ui.components.MedicineImage(p.imageUrl, size = 48.dp)
            }
        },
        headlineContent = { Text(p.productName ?: "Unknown product", maxLines = 2, overflow = TextOverflow.Ellipsis) },
        supportingContent = { Text("${p.quantity ?: 0} units sold", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        trailingContent = { Text(money.format(p.revenue ?: 0.0), style = MaterialTheme.typography.titleSmall, maxLines = 1) },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        modifier = Modifier.fillMaxWidth().shadeRow(onClick = { if (id != null) onOpen(id) })
    )
}
