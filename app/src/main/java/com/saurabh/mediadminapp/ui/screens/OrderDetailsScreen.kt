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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.Order
import com.saurabh.mediadminapp.ui.screens.components.EachOrderCard
import com.saurabh.mediadminapp.utils.ScreensState.ApproveOrderState
import com.saurabh.mediadminapp.utils.cardColors
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OrderDetailsScreen(viewModel: MyViewModel, navController: NavController){
    val response = viewModel.getAllOrderState.collectAsState()
    val isApproveOrder = viewModel.isApproveOrdder.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.getAllOrders()
    }
    LaunchedEffect(response.value.success) {
        response.value.success?.let {
            Log.d("TAG", "OrderDetailsScreen: ${it.message}")
            viewModel.clearGetAllProductState()
            Log.d("TAG", "OrderDetailsScreen: ab recomposition ke baad successfull mai jana chiye")
        }
    }
    Scaffold (modifier = Modifier.background(Color(0xFFffffff))
    ){
        innerpadding ->

        when{
            response.value.isLoading ->{
                Box(
                    modifier = Modifier
                        .padding(innerpadding)
                        .fillMaxSize()

                ) {
                    LoadingScreen(modifier = Modifier)
                    Log.d("TAG", "OrderDetailsScreen: ab recomposition ke baad loading mai aya")

                }
            }
            response.value.error != null ->{
                Box(
                    modifier = Modifier
                        .padding(innerpadding)
                        .fillMaxSize()

                ) {
                    Log.d("TAG", "ProductScreen: error :-> ${response.value.error}")
                    ErrorScreen(
                        errorMessage = response.value.error.toString(),
                    )
                }
            }
            response.value.success != null ->{

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFffffff)),

                ) {
                    Log.d("TAG", "OrderDetailsScreen: ${response.value.success!!.orders}")
                    Log.d("TAG", "OrderDetailsScreen: ab recomposition ke baad successfull mai aya")


                    OrdersListScreen(
                        response.value.success!!.orders,
                        navController,
                        modifier = Modifier,
                        isApproveOrder = isApproveOrder,
                        onApprovalToggle = viewModel::isApproveOrder,
                        viewModel = viewModel
                    )
                }
            }
        }



    }
}

@Composable
fun OrdersListScreen(
    orders: List<Order>,
    navController: NavController,
    modifier: Modifier,
    isApproveOrder: State<Map<String, ApproveOrderState>>,
    onApprovalToggle: (String, Boolean) -> Unit,
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
                FilterStatus.PENDING-> !order.isApproved
                FilterStatus.APPROVED -> order.isApproved
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    // Calculate stats
    val stats = remember(orders) {
        mapOf(
            "total" to orders.size,
            "pending" to orders.count { !it.isApproved },
            "approved" to orders.count { it.isApproved },
            "totalValue" to orders.sumOf { it.total_amount }
        )
    }
    // Medical gradient colors
    val medicalGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF0EA5E9), // sky-500
            Color(0xFF3B82F6)  // blue-500
        )
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                placeholder = { Text("Search orders...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Orders
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["total"].toString(),
                    label = "Total Orders",
                    valueColor = MaterialTheme.colorScheme.onSurface
                )

                // Pending
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["pending"].toString(),
                    label = "Pending",
                    valueColor = Color(0xFFD97706) // yellow-600
                )

                // Approved
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["approved"].toString(),
                    label = "Approved",
                    valueColor = MaterialTheme.colorScheme.primary
                )

                // Total Value
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = "₹${
                        NumberFormat.getInstance(Locale("en", "IN")).format(stats["totalValue"])
                    }",
                    label = "Total Value",
                    valueColor = MaterialTheme.colorScheme.secondary,
                    valueSize = 24.sp
                )
            }
        }
        // search bar

        // status filter
        item {
            val filterOptions = listOf(
                FilterOption(FilterStatus.ALL, "All Users", Icons.AutoMirrored.Filled.List),
                FilterOption(FilterStatus.APPROVED, "Approved", Icons.Default.CheckCircle),
                FilterOption(FilterStatus.PENDING, "Pending", Icons.AutoMirrored.Filled.List)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(filterOptions) { filter ->
                    FilterButton(
                        option = filter,
                        isSelected = filterStatus == filter.key,
                        onClick = { filterStatus = filter.key },
                        medicalGradient = medicalGradient
                    )
                }
            }
        }
        if (filteredOrders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No orders found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }else{
            itemsIndexed (filteredOrders){index,orderItem->
                val bgColor = cardColors[index%cardColors.size]
                EachOrderCard(orderItem,navController,bgColor, modifier = Modifier,isApproveOrder = isApproveOrder,
                    onApprovalToggle = viewModel::isApproveOrder)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEachOrderCard() {
    val sampleOrder = Order(
        id = 1,
        order_id = "ORD001",
        user_id = "USR001",
        product_id = "PRD001",
        product_name = "Paracetamol 500mg",
        user_name = "Dr. Rajesh Kumar",
        quantity = 100,
        price = 25.50,
        total_amount = 2550.00,
        category = "Tablet",
        date_of_order_creation = "2024-01-28",
        _isApproved = false,
        message = "Urgent requirement for hospital ward"

    )

    val dummyApprovalState = remember {
        mutableStateOf(
            mapOf(sampleOrder.order_id to ApproveOrderState(  success= null,
                isLoading = false,
                error = null))
        )
    }

    EachOrderCard(
        order = sampleOrder,
        navController = rememberNavController(),
        bgColor = Color(0xFFE0F7FA),
        modifier = Modifier.padding(16.dp),
        isApproveOrder = dummyApprovalState,
        onApprovalToggle = { orderId, newState ->
            Log.d("Preview", "Approval toggled for $orderId: $newState")
        }
    )
}

