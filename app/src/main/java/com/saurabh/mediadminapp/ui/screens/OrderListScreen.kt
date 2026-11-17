package com.saurabh.mediadminapp.ui.screens

import com.saurabh.mediadminapp.ui.components.GsFilterChip
import com.saurabh.mediadminapp.ui.components.GsTextField
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.core.OrderStatus
import com.saurabh.mediadminapp.data.v1.dto.Order
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.PagedLazyColumn
import com.saurabh.mediadminapp.ui.components.SkeletonList
import com.saurabh.mediadminapp.ui.components.StatusChip
import com.saurabh.mediadminapp.ui.components.MedicineImage
import com.saurabh.mediadminapp.ui.components.shadeRow
import com.saurabh.mediadminapp.ui.components.dateTimeLabel
import com.saurabh.mediadminapp.ui.components.label
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord

private val STATUS_FILTERS = listOf(null, OrderStatus.PENDING, OrderStatus.APPROVED, OrderStatus.SOLD, OrderStatus.CANCELLED, OrderStatus.RETURNED)

/** Orders tab: search + status chips (applied to loaded pages), pull-to-refresh, load-more. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    viewModel: MyViewModel,
    navController: NavController,
    onMenuClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val state by viewModel.getAllOrderState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.getAllOrders() }
    var query by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf<String?>(null) }
    var expanded by rememberSaveable { mutableStateOf(setOf<String>()) }

    val all = state.success?.items
    val shown = remember(all, query, status) {
        all.orEmpty().filter { o ->
            (status == null || o.status.name == status) &&
                (query.isBlank() || o.productName.contains(query, true) || o.userName.contains(query, true) || o.orderId.contains(query, true))
        }
    }

    AppScreenScaffold(
        title = "Orders",
        navigationIcon = { com.saurabh.mediadminapp.ui.components.TileIconButton(Icons.Outlined.Menu, "Menu", onMenuClick, Modifier.padding(start = 4.dp)) },
        scrollBehavior = scrollBehavior,
    ) { inner ->
        PagedLazyColumn(
            isRefreshing = state.isLoading && all != null,
            onRefresh = { viewModel.getAllOrders(force = true) },
            isLoadingMore = state.isLoadingMore,
            endReached = state.endReached,
            onLoadMore = viewModel::loadMoreOrders,
            state = rememberLazyListState(),
            modifier = Modifier.padding(inner).fillMaxSize()
        ) {
            item(key = "search", contentType = "search") {
                GsTextField(
                    value = query, onValueChange = { query = it },
                    placeholder = { Text("Search product, customer or order id") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = if (query.isNotEmpty()) {
                        { IconButton(onClick = { query = "" }) { Icon(Icons.Outlined.Close, contentDescription = "Clear search") } }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item(key = "chips", contentType = "chips") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(STATUS_FILTERS, key = { it?.name ?: "ALL" }) { s ->
                        GsFilterChip(
                            selected = status == s?.name,
                            onClick = { status = s?.name },
                            label = { Text(s?.label() ?: "All") }
                        )
                    }
                }
            }
            when {
                all == null && state.error != null -> item(key = "error", contentType = "state") {
                    ErrorState(message = state.error.orEmpty(), onRetry = { viewModel.getAllOrders(force = true) })
                }
                all == null -> item(key = "skeleton", contentType = "state") { SkeletonList(rows = 6) }
                shown.isEmpty() -> item(key = "empty", contentType = "state") {
                    EmptyState(
                        title = if (all.isEmpty()) "No orders yet" else "No matching orders",
                        message = if (all.isEmpty()) "New customer orders appear here." else "Try another search or status."
                    )
                }
                else -> {
                    val open: (Order) -> Unit = { o ->
                        navController.navigate(Routes.SpecificOrderRoutes.invoke(o.orderId)) { launchSingleTop = true }
                    }
                    shown.groupedByCheckout().forEach { entry ->
                        if (entry.size == 1) {
                            val o = entry.first()
                            item(key = o.orderId, contentType = "order") { OrderRow(o) { open(o) } }
                        } else {
                            val cid = entry.first().checkoutId.orEmpty()
                            item(key = "chk_$cid", contentType = "group") {
                                CheckoutGroupRow(entry, expanded = cid in expanded, onToggle = {
                                    expanded = if (cid in expanded) expanded - cid else expanded + cid
                                })
                            }
                            if (cid in expanded) items(entry, key = { "${cid}_${it.orderId}" }, contentType = { "order" }) { o ->
                                Box(Modifier.padding(start = 16.dp)) { OrderRow(o) { open(o) } }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderRow(o: Order, onClick: () -> Unit) {
    ListItem(
        leadingContent = { MedicineImage(o.imageUrl, size = 56.dp) },
        headlineContent = { Text(o.productName.capitalizeEachWord(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("${o.userName} · ${o.quantity} × ₹%.2f".format(o.price), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(o.createdAt.dateTimeLabel(), style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("₹%.2f".format(o.totalAmount), style = MaterialTheme.typography.titleSmall, maxLines = 1)
                StatusChip(o.status)
            }
        },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        modifier = Modifier.fillMaxWidth().shadeRow(onClick)
    )
}

/** Orders with the same checkoutId (multi-item checkout) become one group; others stay single. Order kept. */
private fun List<Order>.groupedByCheckout(): List<List<Order>> {
    val groups = LinkedHashMap<String, MutableList<Order>>()
    forEach { o -> groups.getOrPut(o.checkoutId?.let { "c:$it" } ?: "o:${o.orderId}") { mutableListOf() } += o }
    return groups.values.toList()
}

/** Status counts, e.g. "2 approved, 1 cancelled". */
internal fun List<Order>.statusSummary(): String =
    groupBy { it.status }.entries.joinToString(", ") { (s, list) -> "${list.size} ${s.label().lowercase()}" }

/** Expandable header row for a multi-item checkout: N items, total, combined status summary. */
@Composable
private fun CheckoutGroupRow(orders: List<Order>, expanded: Boolean, onToggle: () -> Unit) {
    ListItem(
        leadingContent = { MedicineImage(orders.first().imageUrl, size = 56.dp) },
        headlineContent = {
            Text("Checkout · ${orders.size} items", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${orders.first().userName} · ${orders.joinToString { it.productName.capitalizeEachWord() }}",
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(orders.statusSummary(), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("₹%.2f".format(orders.sumOf { it.totalAmount }), style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Icon(
                    if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
        modifier = Modifier.fillMaxWidth().shadeRow(onToggle)
    )
}
