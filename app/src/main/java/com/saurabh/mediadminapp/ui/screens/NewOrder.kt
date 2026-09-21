package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.Order
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayEmptyState
import com.saurabh.mediadminapp.ui.screens.components.ClayErrorScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayFilterChip
import com.saurabh.mediadminapp.ui.screens.components.ClayGradientBackdrop
import com.saurabh.mediadminapp.ui.screens.components.ClayLoadingScreen
import com.saurabh.mediadminapp.ui.screens.components.ClaySearchField
import com.saurabh.mediadminapp.ui.screens.components.ClayStatCard
import com.saurabh.mediadminapp.ui.screens.components.EachOrderCard
import com.saurabh.mediadminapp.ui.screens.components.FilterOption
import com.saurabh.mediadminapp.ui.theme.ClayBadgeApproved
import com.saurabh.mediadminapp.ui.theme.ClayBadgePending
import com.saurabh.mediadminapp.ui.theme.ClayOrderGradient
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary
import com.saurabh.mediadminapp.utils.cardColors
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OrderDetailsScreen1(viewModel: MyViewModel, navController: NavController) {
    val response = viewModel.getAllOrderState.collectAsState()
    val isApproveOrder = viewModel.isApproveOrdder.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.getAllOrders()
    }
    LaunchedEffect(response.value.success) {
        response.value.success?.let {
            viewModel.clearGetAllProductState()
        }
    }

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        ClayGradientBackdrop(gradient = ClayOrderGradient) {
            when {
                response.value.isLoading -> {
                    ClayLoadingScreen(modifier = Modifier.padding(innerPadding))
                }
                response.value.error != null -> {
                    ClayErrorScreen(
                        errorMessage = response.value.error.toString(),
                        modifier = Modifier.padding(innerPadding),
                        onRetry = { viewModel.getAllOrders() }
                    )
                }
                response.value.success != null -> {
                    EnhancedOrdersListScreen(
                        orders = response.value.success!!.orders,
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedOrdersListScreen(
    orders: List<Order>,
    navController: NavController,
    viewModel: MyViewModel
) {
    var searchTerm by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf(FilterStatus.ALL) }
    val isApproveOrder = viewModel.isApproveOrdder.collectAsState()

    val filteredOrders = remember(orders, searchTerm, filterStatus) {
        orders.filter { order ->
            val matchesSearch = order.product_name.contains(searchTerm, ignoreCase = true) ||
                    order.user_name.contains(searchTerm, ignoreCase = true) ||
                    order.order_id.contains(searchTerm, ignoreCase = true)

            val matchesFilter = when (filterStatus) {
                FilterStatus.ALL -> true
                FilterStatus.PENDING -> !order.isApproved
                FilterStatus.APPROVED -> order.isApproved
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    val stats = remember(orders) {
        mapOf(
            "total" to orders.size,
            "pending" to orders.count { !it.isApproved },
            "approved" to orders.count { it.isApproved },
            "totalValue" to orders.sumOf { it.total_amount }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp)
    ) {
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ClayBadgeApproved.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = ClayBadgeApproved,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Orders Queue",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ClayTextPrimary
                                )
                                Text(
                                    text = "${stats["total"]} Registered Orders",
                                    fontSize = 12.sp,
                                    color = ClayTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "TOTAL SUM",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ClayTextSecondary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "₹${NumberFormat.getInstance(Locale.Builder().setLanguage("en").setRegion("IN").build()).format(stats["totalValue"] ?: 0)}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ClayBadgeApproved
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClayStatCard(
                            modifier = Modifier.weight(1f),
                            value = stats["total"].toString(),
                            label = "Total",
                            accentColor = ClayTextPrimary,
                            icon = Icons.Default.List
                        )
                        ClayStatCard(
                            modifier = Modifier.weight(1f),
                            value = stats["pending"].toString(),
                            label = "Pending",
                            accentColor = ClayBadgePending,
                            icon = Icons.Default.HourglassEmpty
                        )
                        ClayStatCard(
                            modifier = Modifier.weight(1f),
                            value = stats["approved"].toString(),
                            label = "Approved",
                            accentColor = ClayBadgeApproved,
                            icon = Icons.Default.CheckCircle
                        )
                    }
                }
            }
        }

        item {
            ClaySearchField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                placeholder = "Search orders...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = ClayPrimary
                    )
                }
            )
        }

        item {
            val filterOptions = listOf(
                FilterOption(FilterStatus.ALL, "All Orders", Icons.Default.List),
                FilterOption(FilterStatus.PENDING, "Pending", Icons.Default.HourglassEmpty),
                FilterOption(FilterStatus.APPROVED, "Approved", Icons.Default.CheckCircle)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(filterOptions) { filter ->
                    ClayFilterChip(
                        label = filter.label,
                        icon = filter.icon,
                        selected = filterStatus == filter.key,
                        onClick = { filterStatus = filter.key },
                        gradient = ClayOrderGradient
                    )
                }
            }
        }

        if (filteredOrders.isEmpty()) {
            item {
                ClayEmptyState(
                    message = "No orders found",
                    subtitle = "Try adjusting your search or filters",
                    emoji = "📦"
                )
            }
        } else {
            itemsIndexed(filteredOrders, key = { _, item -> item.order_id }) { index, orderItem ->
                val bgTint = cardColors[index % cardColors.size]
                EachOrderCard(
                    order = orderItem,
                    navController = navController,
                    bgColor = bgTint,
                    isApproveOrder = isApproveOrder,
                    onApprovalToggle = viewModel::isApproveOrder
                )
            }
        }
    }
}