package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.dto.Sale
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.PagedLazyColumn
import com.saurabh.mediadminapp.ui.components.SkeletonList
import com.saurabh.mediadminapp.ui.components.MedicineImage
import com.saurabh.mediadminapp.ui.components.shadeRow
import com.saurabh.mediadminapp.ui.components.dateTimeLabel
import com.saurabh.mediadminapp.ui.components.dayLabel
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord

/** Sales history grouped by day with sticky headers; pull-to-refresh and load-more. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SalesHistoryScreen(
    viewModel: MyViewModel,
    navController: NavController,
    onMenuClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val state by viewModel.getSellHistory.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.getAllSellHistory() }
    val all = state.success?.items
    val groups = remember(all) { all.orEmpty().groupBy { it.soldAt.dayLabel() } }

    AppScreenScaffold(
        title = "Sales history",
        navigationIcon = { com.saurabh.mediadminapp.ui.components.TileIconButton(Icons.Outlined.Menu, "Menu", onMenuClick, Modifier.padding(start = 4.dp)) },
        scrollBehavior = scrollBehavior,
    ) { inner ->
        PagedLazyColumn(
            isRefreshing = state.isLoading && all != null,
            onRefresh = { viewModel.getAllSellHistory(force = true) },
            isLoadingMore = state.isLoadingMore,
            endReached = state.endReached,
            onLoadMore = viewModel::loadMoreSellHistory,
            state = rememberLazyListState(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(inner).fillMaxSize()
        ) {
            when {
                all == null && state.error != null -> item(key = "error", contentType = "state") {
                    ErrorState(message = state.error.orEmpty(), onRetry = { viewModel.getAllSellHistory(force = true) })
                }
                all == null -> item(key = "skeleton", contentType = "state") { SkeletonList(rows = 6) }
                all.isEmpty() -> item(key = "empty", contentType = "state") {
                    EmptyState(title = "No sales yet", message = "Completed sales appear here.")
                }
                else -> groups.forEach { (day, sales) ->
                    stickyHeader(key = "h_$day", contentType = "header") {
                        Text(
                            day,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(vertical = 8.dp)
                        )
                    }
                    items(sales.size, key = { sales[it].saleId }, contentType = { "sale" }) { i ->
                        SaleRow(sales[i]) { navController.navigate(Routes.SpecificOrderRoutes.invoke(sales[i].orderId)) { launchSingleTop = true } }
                    }
                }
            }
        }
    }
}

@Composable
private fun SaleRow(s: Sale, onClick: () -> Unit) {
    ListItem(
        leadingContent = { MedicineImage(s.imageUrl, size = 56.dp) },
        headlineContent = { Text(s.productName.capitalizeEachWord(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("${s.userName} · ${s.quantity} × ₹%.2f".format(s.price), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(s.soldAt.dateTimeLabel() + " · batch ${s.batchNo}", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        trailingContent = { Text("₹%.2f".format(s.totalAmount), style = MaterialTheme.typography.titleSmall, maxLines = 1) },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        modifier = Modifier.fillMaxWidth().shadeRow(onClick)
    )
}
