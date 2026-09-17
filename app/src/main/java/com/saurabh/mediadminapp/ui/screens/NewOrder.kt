package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.Order
import com.saurabh.mediadminapp.ui.screens.components.ClayEmptyState
import com.saurabh.mediadminapp.ui.screens.components.ClayErrorScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayFilterChip
import com.saurabh.mediadminapp.ui.screens.components.FilterOption
import com.saurabh.mediadminapp.ui.screens.components.ClayLoadingScreen
import com.saurabh.mediadminapp.ui.screens.components.ClaySearchField
import com.saurabh.mediadminapp.ui.screens.components.ClayStatCard
import com.saurabh.mediadminapp.ui.screens.components.EachOrderCard
import com.saurabh.mediadminapp.ui.theme.ClayOrderGradient
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayScreenBg
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
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
    
    Scaffold(modifier = Modifier.background(ClayScreenBg)) { innerpadding ->
        when {
            response.value.isLoading -> {
                ClayLoadingScreen(modifier = Modifier.padding(innerpadding))
            }
            response.value.error != null -> {
                ClayErrorScreen(
                    errorMessage = response.value.error.toString(),
                    modifier = Modifier.padding(innerpadding)
                )
            }
            response.value.success != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ClayScreenBg)
                ) {
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
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            ClaySearchField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                placeholder = "Search orders...",
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClayStatCard(
                    modifier = Modifier.weight(1f),
                    value = stats["total"].toString(),
                    label = "Total",
                    accentColor = ClayTextPrimary
                )
                ClayStatCard(
                    modifier = Modifier.weight(1f),
                    value = stats["pending"].toString(),
                    label = "Pending",
                    accentColor = Color(0xFFD97706)
                )
                ClayStatCard(
                    modifier = Modifier.weight(1f),
                    value = stats["approved"].toString(),
                    label = "Approved",
                    accentColor = ClayPrimary
                )
                ClayStatCard(
                    modifier = Modifier.weight(1f),
                    value = "₹${NumberFormat.getInstance(Locale.Builder().setLanguage("en").setRegion("IN").build()).format(stats["totalValue"])}",
                    label = "Total ₹",
                    accentColor = Color(0xFF10B981)
                )
            }
        }

        item {
            val filterOptions = listOf(
                FilterOption(FilterStatus.ALL, "All", Icons.Default.List),
                FilterOption(FilterStatus.APPROVED, "Approved", Icons.Default.CheckCircle),
                FilterOption(FilterStatus.PENDING, "Pending", Icons.Default.List)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
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
                    emoji = "📦"
                )
            }
        } else {
            itemsIndexed(filteredOrders) { index, orderItem ->
                val bgColor = cardColors[index % cardColors.size]
                EachOrderCard(
                    order = orderItem,
                    navController = navController,
                    bgColor = bgColor,
                    isApproveOrder = isApproveOrder,
                    onApprovalToggle = viewModel::isApproveOrder
                )
            }
        }
    }
}