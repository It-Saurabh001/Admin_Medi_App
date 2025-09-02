package com.saurabh.mediadminapp.ui.screens.nav




import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import java.util.*
import kotlin.math.roundToInt


// Data classes
data class Sale(
    val id: String,
    val sellId: String,
    val productId: String,
    val productName: String,
    val userId: String,
    val userName: String,
    val orderId: String,
    val quantity: Int,
    val price: Double,
    val totalAmount: Double,
    val remainingStock: Int,
    val dateOfSell: String,
    val isApproved: Boolean
)

// Mock data
val mockSales = listOf(
    Sale(
        id = "1",
        sellId = "SL001",
        productId = "PRD001",
        productName = "Paracetamol 500mg",
        userId = "USR001",
        userName = "Dr. Rajesh Kumar",
        orderId = "ORD001",
        quantity = 100,
        price = 25.50,
        totalAmount = 2550.00,
        remainingStock = 50,
        dateOfSell = "2024-01-29",
        isApproved = true
    ),
    Sale(
        id = "2",
        sellId = "SL002",
        productId = "PRD002",
        productName = "Amoxicillin Capsules",
        userId = "USR002",
        userName = "Priya Sharma",
        orderId = "ORD002",
        quantity = 50,
        price = 45.00,
        totalAmount = 2250.00,
        remainingStock = 8,
        dateOfSell = "2024-01-30",
        isApproved = true
    ),
    Sale(
        id = "3",
        sellId = "SL003",
        productId = "PRD003",
        productName = "Vitamin D3 Tablets",
        userId = "USR004",
        userName = "Dr. Sarah Johnson",
        orderId = "ORD004",
        quantity = 200,
        price = 35.00,
        totalAmount = 7000.00,
        remainingStock = 0,
        dateOfSell = "2024-01-31",
        isApproved = false
    )
)

// Filter data class
data class PeriodFilter(
    val key: String,
    val label: String,
    val icon: ImageVector
)
@Preview
@Composable
fun SalesScreen(
    onViewSale: (String) -> Unit = {},
    onAnalyzeSale: (String) -> Unit = {},
    onShowSalesTrends: () -> Unit = {},
    onShowAnalyticsReport: () -> Unit = {}
) {
    var sales by remember { mutableStateOf(mockSales) }
    var searchTerm by remember { mutableStateOf("") }
    var filterPeriod by remember { mutableStateOf("all") }

    // Filter sales based on search term
    val filteredSales = remember(sales, searchTerm, filterPeriod) {
        sales.filter { sale ->
            val matchesSearch = sale.productName.contains(searchTerm, ignoreCase = true) ||
                    sale.userName.contains(searchTerm, ignoreCase = true) ||
                    sale.sellId.contains(searchTerm, ignoreCase = true)

            // For demo purposes, we'll just filter by search term
            // In real app, you'd filter by actual date ranges
            matchesSearch
        }
    }

    // Calculate stats
    val stats = remember(sales) {
        val totalRevenue = sales.sumOf { it.totalAmount }
        val averageOrderValue = if (sales.isNotEmpty()) totalRevenue / sales.size else 0.0

        mapOf(
            "totalSales" to sales.size,
            "totalRevenue" to totalRevenue,
            "completedSales" to sales.count { it.isApproved },
            "averageOrderValue" to averageOrderValue
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
        // Header with Main Stats (2 columns)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Revenue
                MainStatsCard(
                    modifier = Modifier.weight(1f),
                    value = "₹${NumberFormat.getInstance(Locale("en", "IN")).format(stats["totalRevenue"])}",
                    label = "Total Revenue",
                    valueColor = MaterialTheme.colorScheme.primary
                )

                // Total Sales
                MainStatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["totalSales"].toString(),
                    label = "Total Sales",
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Secondary Stats (2 columns)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Completed Sales
                SecondaryStatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["completedSales"].toString(),
                    label = "Completed",
                    valueColor = MaterialTheme.colorScheme.secondary
                )

                // Average Order Value
                SecondaryStatsCard(
                    modifier = Modifier.weight(1f),
                    value = "₹${NumberFormat.getInstance(Locale("en", "IN")).format((stats["averageOrderValue"] as Double).roundToInt())}",
                    label = "Avg Order Value",
                    valueColor = Color(0xFF9333EA) // purple-600
                )
            }
        }

        // Search
        item {
            OutlinedTextField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                placeholder = { Text("Search sales...") },
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

        // Period Filter
        item {
            val filterOptions = listOf(
                PeriodFilter("all", "All Time", Icons.Default.List),
                PeriodFilter("today", "Today", Icons.Default.DateRange),
                PeriodFilter("week", "This Week", Icons.Default.DateRange),
                PeriodFilter("month", "This Month", Icons.Default.Home)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(filterOptions) { filter ->
                    PeriodFilterButton(
                        option = filter,
                        isSelected = filterPeriod == filter.key,
                        onClick = { filterPeriod = filter.key },
                        medicalGradient = medicalGradient
                    )
                }
            }
        }

        // Analytics Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShowSalesTrends,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Sales Trends",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sales Trends")
                }

                OutlinedButton(
                    onClick = onShowAnalyticsReport,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Analytics Report",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analytics Report")
                }
            }
        }

        // Sales List
        if (filteredSales.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "No sales",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No sales found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredSales) { sale ->
                SalesCard(
                    sale = sale,
                    onView = { onViewSale(sale.id) },
                    onAnalyze = { onAnalyzeSale(sale.id) }
                )
            }
        }
    }
}

@Composable
private fun MainStatsCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    valueColor: Color
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
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

@Composable
private fun SecondaryStatsCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    valueColor: Color
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
                fontSize = 20.sp,
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

@Composable
 fun PeriodFilterButton(
    option: PeriodFilter,
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
fun SalesCard(
    sale: Sale,
    onView: () -> Unit,
    onAnalyze: () -> Unit
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
                    text = sale.sellId,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                // Status Badge
                Surface(
                    color = if (sale.isApproved) Color(0xFF10B981) else Color(0xFFF59E0B),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (sale.isApproved) "Completed" else "Pending",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Product Info
            Text(
                text = sale.productName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Sold to: ${sale.userName}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Order and Quantity Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Order: ${sale.orderId}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Qty: ${sale.quantity}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount and Stock Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${NumberFormat.getInstance(Locale("en", "IN")).format(sale.totalAmount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Stock Status
                Text(
                    text = if (sale.remainingStock == 0) "Out of Stock" else "Stock: ${sale.remainingStock}",
                    fontSize = 12.sp,
                    color = if (sale.remainingStock == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Date
            Text(
                text = "Sold on: ${sale.dateOfSell}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "View",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View")
                }

                Button(
                    onClick = onAnalyze,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C3AED) // purple-600
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Analyze",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Analyze")
                }
            }
        }
    }
}