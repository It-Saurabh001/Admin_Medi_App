package com.saurabh.mediadminapp.ui.screens.nav

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.RemoveShoppingCart
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.CurrencyRupee
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.core.UiState
import com.saurabh.mediadminapp.data.v1.dto.DashboardSummary
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveContent
import com.saurabh.mediadminapp.ui.adaptive.adaptiveGridCells
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.SectionHeader
import com.saurabh.mediadminapp.ui.components.SkeletonList
import com.saurabh.mediadminapp.ui.components.StatCard
import java.text.NumberFormat
import java.util.Locale

private data class DashTile(
    val key: String,
    val label: String,
    val value: String,
    val icon: ImageVector,
    val route: String,
    val highlight: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MyViewModel,
    navController: NavController,
    onMenuClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val state by viewModel.dashboardSummaryState.collectAsStateWithLifecycle()
    // Keep the last numbers on screen while refreshing or after a failed refresh.
    var lastData by remember { mutableStateOf<DashboardSummary?>(null) }
    LaunchedEffect(state) { (state as? UiState.Success)?.let { lastData = it.data } }
    LaunchedEffect(Unit) { viewModel.getDashboardSummary() }

    // Card taps open tab roots with tab semantics (saved state, single top).
    val openTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(Routes.HomeRoutes.route) { saveState = true }
        }
    }

    AppScreenScaffold(
        title = "Dashboard",
        navigationIcon = {
            IconButton(onClick = onMenuClick) { Icon(Icons.Outlined.Menu, contentDescription = "Menu") }
        },
        actions = {
            IconButton(onClick = { navController.navigate(Routes.ReportsRoutes.route) { launchSingleTop = true } }) {
                Icon(Icons.Outlined.Insights, contentDescription = "Sales report")
            }
        },
        scrollBehavior = scrollBehavior,
    ) { inner ->
        val s = state
        val data = (s as? UiState.Success)?.data ?: lastData
        PullToRefreshBox(
            isRefreshing = s is UiState.Loading && data != null,
            onRefresh = { viewModel.getDashboardSummary() },
            modifier = Modifier.padding(inner).fillMaxSize()
        ) {
            when {
                data != null -> DashboardGrid(
                    d = data,
                    refreshError = (s as? UiState.Error)?.error?.userMessage(),
                    onOpen = openTab
                )
                s is UiState.Error -> ErrorState(
                    message = s.error.userMessage(),
                    onRetry = { viewModel.getDashboardSummary() }
                )
                // Loading, or the initial Empty before the first request lands.
                else -> AdaptiveContent { SkeletonList(rows = 4) }
            }
        }
    }
}

@Composable
private fun DashboardGrid(d: DashboardSummary, refreshError: String?, onOpen: (String) -> Unit) {
    val money = remember {
        NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("en").setRegion("IN").build())
            .apply { maximumFractionDigits = 2 }
    }
    // InventoryAlerts screen not built yet: stock tiles open Products for now.
    val inventoryRoute = Routes.ProductRoutes.route
    val orders = listOf(
        DashTile("pending", "Pending orders", d.pendingOrders.toString(), Icons.Outlined.PendingActions, Routes.OrdersRoutes.route, highlight = d.pendingOrders > 0),
        DashTile("approved", "Approved orders", d.approvedOrders.toString(), Icons.Outlined.CheckCircle, Routes.OrdersRoutes.route),
        DashTile("salesTotal", "Today sales", money.format(d.todaySalesTotal), Icons.Outlined.CurrencyRupee, Routes.HistoryRoutes.route),
        DashTile("salesCount", "Sales today", d.todaySalesCount.toString(), Icons.Outlined.Inventory2, Routes.HistoryRoutes.route),
    )
    val stock = listOf(
        DashTile("products", "Products", d.products.toString(), Icons.Outlined.Medication, Routes.ProductRoutes.route),
        DashTile("low", "Low stock", d.lowStock.toString(), Icons.Outlined.WarningAmber, inventoryRoute, highlight = d.lowStock > 0),
        DashTile("out", "Out of stock", d.outOfStock.toString(), Icons.Outlined.RemoveShoppingCart, inventoryRoute, highlight = d.outOfStock > 0),
        DashTile("near", "Near expiry (${d.nearExpiryDays} d)", d.nearExpiryBatches.toString(), Icons.Outlined.HourglassTop, inventoryRoute, highlight = d.nearExpiryBatches > 0),
        DashTile("expired", "Expired batches", d.expiredBatches.toString(), Icons.Outlined.EventBusy, inventoryRoute, highlight = d.expiredBatches > 0),
    )

    AdaptiveContent {
        LazyVerticalGrid(
            columns = adaptiveGridCells(160.dp),
            state = rememberLazyGridState(),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (refreshError != null) {
                item(key = "refreshError", span = { GridItemSpan(maxLineSpan) }, contentType = "banner") {
                    Text(
                        text = "Showing last loaded data. $refreshError",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            d.date?.let { date ->
                item(key = "date", span = { GridItemSpan(maxLineSpan) }, contentType = "caption") {
                    Text(
                        text = "As of $date",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            item(key = "hOrders", span = { GridItemSpan(maxLineSpan) }, contentType = "header") {
                SectionHeader("Orders & sales")
            }
            items(orders, key = { it.key }, contentType = { "stat" }) { t ->
                StatCard(t.label, t.value, t.icon, onClick = { onOpen(t.route) }, modifier = Modifier.fillMaxWidth(), highlight = t.highlight)
            }
            item(key = "hStock", span = { GridItemSpan(maxLineSpan) }, contentType = "header") {
                SectionHeader("Inventory")
            }
            items(stock, key = { it.key }, contentType = { "stat" }) { t ->
                StatCard(t.label, t.value, t.icon, onClick = { onOpen(t.route) }, modifier = Modifier.fillMaxWidth(), highlight = t.highlight)
            }
        }
    }
}
