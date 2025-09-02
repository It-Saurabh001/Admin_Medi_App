package com.saurabh.mediadminapp.ui.screens.nav


import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import java.text.NumberFormat
import java.util.Locale

// Data classes
data class Order(
    val id: String,
    val orderId: String,
    val userId: String,
    val productId: String,
    val productName: String,
    val userName: String,
    val quantity: Int,
    val price: Double,
    val totalAmount: Double,
    val category: String,
    val dateOfOrderCreation: String,
    val isApproved: Boolean,
    val message: String
)

// Mock data
val mockOrders = listOf(
    Order(
        id = "1",
        orderId = "ORD001",
        userId = "USR001",
        productId = "PRD001",
        productName = "Paracetamol 500mg",
        userName = "Dr. Rajesh Kumar",
        quantity = 100,
        price = 25.50,
        totalAmount = 2550.00,
        category = "Tablet",
        dateOfOrderCreation = "2024-01-28",
        isApproved = false,
        message = "Urgent requirement for hospital ward"
    ),
    Order(
        id = "2",
        orderId = "ORD002",
        userId = "USR002",
        productId = "PRD002",
        productName = "Amoxicillin Capsules",
        userName = "Priya Sharma",
        quantity = 50,
        price = 45.00,
        totalAmount = 2250.00,
        category = "Capsule",
        dateOfOrderCreation = "2024-01-29",
        isApproved = true,
        message = ""
    ),
    Order(
        id = "3",
        orderId = "ORD003",
        userId = "USR003",
        productId = "PRD003",
        productName = "Insulin Injection",
        userName = "Dr. Amit Singh",
        quantity = 20,
        price = 150.00,
        totalAmount = 3000.00,
        category = "Injection",
        dateOfOrderCreation = "2024-01-30",
        isApproved = false,
        message = "Required for diabetic patients in ICU"
    )
)

// Filter data class
data class FilterOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

@Preview(showSystemUi = true)
@Composable
fun OrdersScreen(
    onViewOrder: (String) -> Unit = {},
    onApproveOrder: (String) -> Unit = {},
    onRejectOrder: (String) -> Unit = {},
) {




    var orders by remember { mutableStateOf(mockOrders) }
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
                    valueSize = 18.sp
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
                FilterOption("all", "All Orders", Icons.AutoMirrored.Filled.List),
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

        // Create New Order Button
        item {
            Button(
                onClick = { /* Handle create new order */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(medicalGradient, RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create New Order")
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
                OrderCard(
                    order = order,
                    onView = { onViewOrder(order.id) },
                    onApprove = { onApproveOrder(order.id) },
                    onReject = { onRejectOrder(order.id) }
                )
            }
        }
    }
}

@Composable
fun StatsCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    valueColor: Color,
    valueSize: androidx.compose.ui.unit.TextUnit = 24.sp
) {
    val state = rememberScrollState()
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
                modifier= Modifier
                    .horizontalScroll(state),
                fontSize = valueSize,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

        }
    }
}

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
fun OrderCard(
    order: Order,
    onView: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp)),
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

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onView,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,  //Visibility
                        contentDescription = "View",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View")
                }

                if (!order.isApproved) {
                    Button(
                        onClick = onApprove,
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

                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Reject",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject")
                    }
                }
            }
        }
    }
}