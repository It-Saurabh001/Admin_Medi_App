package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.dto.Product
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.LoadingState
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    viewModel: MyViewModel,
    navController: NavController,
    onMenuClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
) {
    val state by viewModel.getAllProduct.collectAsState()
    LaunchedEffect(Unit) { viewModel.getAllProduct() }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Products", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) { Icon(Icons.Outlined.Menu, contentDescription = "Menu") }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.AddProductRoutes.route) }) {
                Icon(Icons.Outlined.Add, contentDescription = "Add product")
            }
        }
    ) { inner ->
        val data = state.success
        PullToRefreshBox(
            isRefreshing = state.isLoading && data != null,
            onRefresh = { viewModel.getAllProduct(force = true) },
            modifier = Modifier.padding(inner).fillMaxSize()
        ) {
            when {
                state.isLoading && data == null -> LoadingState()
                state.error != null && data == null ->
                    ErrorState(message = state.error.orEmpty(), onRetry = { viewModel.getAllProduct(force = true) })
                data != null -> ProductList(
                    products = data.items,
                    isLoadingMore = state.isLoadingMore,
                    onLoadMore = { viewModel.loadMoreProducts() },
                    onOpen = { navController.navigate(Routes.SpecificProductRoutes.invoke(it.productId)) }
                )
                else -> LoadingState()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductList(
    products: List<Product>,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    onOpen: (Product) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var schedule by rememberSaveable { mutableStateOf<String?>(null) }
    var inStockOnly by rememberSaveable { mutableStateOf(false) }
    var activeOnly by rememberSaveable { mutableStateOf(false) }

    val categories = remember(products) { products.map { it.category }.filter { it.isNotBlank() }.distinct() }
    val schedules = remember(products) { products.map { it.scheduleType }.filter { it.isNotBlank() }.distinct() }
    val filtered = remember(products, query, category, schedule, inStockOnly, activeOnly) {
        products.filter { p ->
            (query.isBlank() || p.name.contains(query, true) || p.genericName.contains(query, true) ||
                    p.brandName.contains(query, true) || p.barcode.contains(query, true)) &&
                    (category == null || p.category.equals(category, true)) &&
                    (schedule == null || p.scheduleType == schedule) &&
                    (!inStockOnly || p.inStock) &&
                    (!activeOnly || p.isActive)
        }
    }

    val listState = rememberLazyListState()
    val nearEnd by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= listState.layoutInfo.totalItemsCount - 3
        }
    }
    LaunchedEffect(nearEnd, filtered.size) { if (nearEnd) onLoadMore() }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "search") {
            DockedSearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = { Text("Search products") },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) }
                    )
                },
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier.fillMaxWidth()
            ) {}
        }
        item(key = "filters") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories, key = { "c$it" }) { c ->
                    FilterChip(
                        selected = category == c,
                        onClick = { category = if (category == c) null else c },
                        label = { Text(c.capitalizeEachWord(), maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
                items(schedules, key = { "s$it" }) { s ->
                    FilterChip(
                        selected = schedule == s,
                        onClick = { schedule = if (schedule == s) null else s },
                        label = { Text(s, maxLines = 1) }
                    )
                }
                item(key = "instock") {
                    FilterChip(selected = inStockOnly, onClick = { inStockOnly = !inStockOnly }, label = { Text("In stock") })
                }
                item(key = "active") {
                    FilterChip(selected = activeOnly, onClick = { activeOnly = !activeOnly }, label = { Text("Active") })
                }
            }
        }
        if (filtered.isEmpty()) {
            item(key = "empty") {
                EmptyState(title = "No products", message = "Try a different search or filter.")
            }
        } else {
            items(filtered, key = { it.productId }) { p -> ProductRow(p) { onOpen(p) } }
        }
        if (isLoadingMore) {
            item(key = "more") {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductRow(p: Product, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
            leadingContent = {
                if (!p.imageUrl.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(p.imageUrl),
                        contentDescription = p.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.small)
                    )
                } else {
                    Icon(Icons.Outlined.Inventory2, contentDescription = null, modifier = Modifier.size(56.dp))
                }
            },
            headlineContent = {
                Text(p.name.capitalizeEachWord(), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            supportingContent = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        listOf(p.strength, p.packSize).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { p.category.capitalizeEachWord() },
                        style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChip(
                            onClick = onClick,
                            label = {
                                Text(
                                    when {
                                        !p.inStock -> "Out of stock"
                                        p.stock <= p.reorderLevel -> "Low · ${p.stock}"
                                        else -> "Stock ${p.stock}"
                                    },
                                    maxLines = 1
                                )
                            }
                        )
                        if (p.requiresPrescription) {
                            AssistChip(onClick = onClick, label = { Text("Rx", maxLines = 1) })
                        }
                    }
                }
            }
        )
    }
}
