package com.saurabh.mediadminapp.ui.screens

import android.util.Log
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.nav.EachUserOrderRoutes
import com.saurabh.mediadminapp.ui.screens.nav.mockOrders
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OrderDetailsScreen1(viewModel: MyViewModel, navController: NavController){
    val response = viewModel.getAllOrderState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.getAllOrders()
    }
    LaunchedEffect(response.value.success) {
        response.value.success?.let {
            Log.d("TAG", "OrderDetailsScreen: ${it.message}")
            viewModel.clearGetAllProductState()
        }
    }

    Scaffold {
            innerpadding ->

        when{
            response.value.isLoading ->{
                Box(
                    modifier = Modifier
                        .padding(innerpadding)
                        .fillMaxSize()

                ) {
                    LoadingScreen(modifier = Modifier)
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

                ) {

                    EnhancedOrdersListScreen(
                        mockOrders,
                        navController,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun previews() {
    val navController = rememberNavController()
    EnhancedOrdersListScreen(
        orders = mockOrders,
        navController = navController // Replace with actual NavController in real use
    )

}


@Composable
fun EnhancedOrdersListScreen(orders: List<com.saurabh.mediadminapp.ui.screens.nav.Order>, navController: NavController) {
    var searchTerm by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("all") }

    // Filter orders based on search and status
    val filteredOrders = remember(orders, searchTerm, filterStatus) {
        orders.filter { order ->
            val matchesSearch = order.productName.contains(searchTerm, ignoreCase = true) ||
                    order.userName.contains(searchTerm, ignoreCase = true) ||
                    order.orderId.contains(searchTerm, ignoreCase = true)

            val matchesFilter = when (filterStatus) {
                "all" -> true
                "pending" -> !order.isApproved
                "approved" -> order.isApproved
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
            "totalValue" to orders.sumOf { it.totalAmount }
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
        // Header with Stats
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
                    value = "₹${NumberFormat.getInstance(Locale("en", "IN")).format(stats["totalValue"])}",
                    label = "Total Value",
                    valueColor = MaterialTheme.colorScheme.secondary,
                    valueSize = 16.sp
                )
            }
        }

        // Search
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

        // Status Filter
        item {
            val filterOptions = listOf(
                FilterOption("all", "All Orders", Icons.Default.List),
                FilterOption("pending", "Pending", Icons.Default.CheckCircle),
                FilterOption("approved", "Approved", Icons.Default.CheckCircle)
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

        // Orders List
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
        } else {
            items(filteredOrders) { order ->
                EnhancedOrderCard(
                    order = order,
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun StatsCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    valueColor: Color,
    valueSize: androidx.compose.ui.unit.TextUnit = 24.sp
) {
    Card(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = valueSize,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Filter data class
data class FilterOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun FilterButton(
    option: FilterOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    medicalGradient: Brush
) {
    val buttonColors = if (isSelected) {
        ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    } else {
        ButtonDefaults.outlinedButtonColors()
    }

    val modifier = if (isSelected) {
        Modifier.background(medicalGradient, RoundedCornerShape(20.dp))
    } else {
        Modifier
    }

    if (isSelected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            colors = buttonColors,
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = option.label,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = option.label,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun EnhancedOrderCard(
    order: com.saurabh.mediadminapp.ui.screens.nav.Order,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp))
        ,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderId,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                // Status Badge
                Surface(
                    color = if (order.isApproved) Color(0xFF10B981) else Color(0xFFF59E0B),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (order.isApproved) "Approved" else "Pending",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Product Info
            Text(
                text = order.productName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "By: ${order.userName}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // User ID and Product ID Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "User ID: ${order.userId}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Product ID: ${order.productId}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quantity and Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Qty: ${order.quantity} ${order.category}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "₹${NumberFormat.getInstance(Locale("en", "IN")).format(order.totalAmount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Date
            Text(
                text = "Ordered: ${order.dateOfOrderCreation}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Message (if exists)
            if (order.message.isNotEmpty()) {
                Text(
                    text = order.message,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp)
                )
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigate(EachUserOrderRoutes.invoke(order.userId)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "View",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Details")
                }

                if (!order.isApproved) {
                    Button(
                        onClick = { /* Handle approve action */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Approve",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve")
                    }
                } else {
                    Button(
                        onClick = { /* Handle view approved order */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Approved",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approved")
                    }
                }
            }
        }
    }
}