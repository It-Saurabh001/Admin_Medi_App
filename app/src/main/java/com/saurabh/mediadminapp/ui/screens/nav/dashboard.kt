package com.saurabh.mediadminapp.ui.screens.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview(showBackground = true)
@Composable
fun Dashboard() {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .background(Color(0xFFE5E5E5))) {

        // Header
        DashboardHeader()

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Cards
        StatsCards(stats = mockDashboardStats)

        Spacer(modifier = Modifier.height(16.dp))

        // Sales Chart
        SalesChart(data = mockChartData)

        Spacer(modifier = Modifier.height(16.dp))

        // Product Sales Cards
        ProductSalesCards(productSales = mockProductSales)

        Spacer(modifier = Modifier.height(16.dp))

        // Product List
        ProductList(products = mockProducts)
    }
}

@Composable
fun DashboardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon and title
            Icon(Icons.Default.AccountBox, contentDescription = "Dashboard", modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "Dashboard", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = "Sales & Inventory", style = MaterialTheme.typography.bodyMedium)
            }
        }
        IconButton(onClick = { /* Refresh logic */ }) {
            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
        }
    }
}

@Composable
fun StatsCards(stats: DashboardStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatCard(
            icon = Icons.Default.ShoppingCart,
            title = "Today Sales",
            value = "$${stats.todaySales}",
            growth = stats.salesGrowth.toFloat()
        )
        StatCard(
            icon = Icons.Default.ShoppingCart,
            title = "Total Products",
            value = stats.totalProducts.toString(),
            growth = 0f // No growth for this one
        )
        StatCard(
            icon = Icons.Default.Warning,
            title = "Low Stock",
            value = stats.lowStockProducts.toString(),
            growth = 0f
        )
        StatCard(
            icon = Icons.Default.Close,
            title = "Out of Stock",
            value = stats.outOfStockProducts.toString(),
            growth = 0f
        )
    }
}

@Composable
fun StatCard(icon: ImageVector, title: String, value: String, growth: Float) {
    Card(
        modifier = Modifier
//            .weight(1f)
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(24.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 4.dp))
            if (growth != 0f) {
                Text(
                    text = "+$growth%",
                    color = Color.Green,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
@Composable
fun ProductList(products: List<Product>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Product Inventory", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(products) { product ->
                    ProductItem(product)
                }
            }
        }
    }
}

@Composable
fun ProductItem(product: Product) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFFF5F5F5)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (product.status) {
                    "in-stock" -> Icons.Default.CheckCircle
                    "low-stock" -> Icons.Default.Warning
                    "out-of-stock" -> Icons.Default.Close
                    else -> Icons.Default.CheckCircle
                },
                contentDescription = "Product Status",
                tint = when (product.status) {
                    "in-stock" -> Color.Green
                    "low-stock" -> Color.Yellow
                    "out-of-stock" -> Color.Red
                    else -> Color.Gray
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = product.name, fontWeight = FontWeight.Bold)
                Text(text = "${product.category} • $${product.price}")
            }
        }
        Text(text = "${product.quantity} units")
    }
}
@Composable
fun SalesChart(data: List<ChartData>) {
    // Placeholder for chart UI - ideally integrate a charting library here
    Column {
        Text(text = "Sales History", fontWeight = FontWeight.Bold)
        // Placeholder for the chart display
        Text(text = "Chart data goes here")
    }
}
@Composable
fun FilterTabs(selectedFilter: String, onFilterChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        TextButton(onClick = { onFilterChange("day") }) {
            Text(text = "Day", color = if (selectedFilter == "day") Color.Blue else Color.Gray)
        }
        TextButton(onClick = { onFilterChange("month") }) {
            Text(text = "Month", color = if (selectedFilter == "month") Color.Blue else Color.Gray)
        }
        TextButton(onClick = { onFilterChange("year") }) {
            Text(text = "Year", color = if (selectedFilter == "year") Color.Blue else Color.Gray)
        }
    }
}

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val quantity: Int,
    val minStock: Int,
    val price: Double,
    val status: String
)

data class SellHistoryItem(
    val orderId: String,
    val sellId: String,
    val dateOfSell: String,
    val id: Int,
    val isApproved: Boolean,
    val price: Double,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val remainingStock: Int,
    val totalAmount: Double,
    val userId: String,
    val userName: String
)

data class ProductSales(
    val productId: String,
    val productName: String,
    val totalQuantity: Int,
    val totalAmount: Double,
    val remainingStock: Int,
    val salesCount: Int
)

data class DashboardStats(
    val totalProducts: Int,
    val lowStockProducts: Int,
    val outOfStockProducts: Int,
    val totalSales: Double,
    val todaySales: Double,
    val salesGrowth: Double
)

data class ChartData(
    val date: String,
    val sales: Double,
    val quantity: Int
)

val mockProducts = listOf(
    Product(
        id = "1",
        name = "Wireless Headphones",
        category = "Electronics",
        quantity = 45,
        minStock = 10,
        price = 89.99,
        status = "in-stock"
    ),
    Product(
        id = "2",
        name = "Smartphone Case",
        category = "Accessories",
        quantity = 8,
        minStock = 15,
        price = 29.99,
        status = "low-stock"
    ),
    Product(
        id = "3",
        name = "Bluetooth Speaker",
        category = "Electronics",
        quantity = 0,
        minStock = 5,
        price = 149.99,
        status = "out-of-stock"
    ),
    Product(
        id = "4",
        name = "USB Cable",
        category = "Accessories",
        quantity = 120,
        minStock = 20,
        price = 19.99,
        status = "in-stock"
    ),
    Product(
        id = "5",
        name = "Power Bank",
        category = "Electronics",
        quantity = 32,
        minStock = 10,
        price = 59.99,
        status = "in-stock"
    )
)

val mockSellHistory = listOf(
    SellHistoryItem(
        orderId = "ORDfec7b125",
        sellId = "SELL28036cef",
        dateOfSell = "2025-07-12",
        id = 1,
        isApproved = true,
        price = 35.0,
        productId = "PROD_49185eb0",
        productName = "Amoxicillin",
        quantity = 25,
        remainingStock = 95,
        totalAmount = 875.0,
        userId = "59e359d8-4ae3-4134-b0f6-c5e5d59eefb0",
        userName = "Saurabh Yadav"
    ),
    SellHistoryItem(
        orderId = "ORDa02bf4be",
        sellId = "SELLf9670779",
        dateOfSell = "2025-07-12",
        id = 3,
        isApproved = true,
        price = 20.0,
        productId = "PROD_a9970328",
        productName = "Salicylic Acid",
        quantity = 25,
        remainingStock = 60,
        totalAmount = 500.0,
        userId = "59e359d8-4ae3-4134-b0f6-c5e5d59eefb0",
        userName = "Saurabh Yadav"
    ),
    // ... Add other sell history items here
)

val mockProductSales = listOf(
    ProductSales(
        productId = "PROD_49185eb0",
        productName = "Amoxicillin",
        totalQuantity = 55,
        totalAmount = 1925.0,
        remainingStock = 40,
        salesCount = 2
    ),
    ProductSales(
        productId = "PROD_a9970328",
        productName = "Salicylic Acid",
        totalQuantity = 55,
        totalAmount = 1100.0,
        remainingStock = 30,
        salesCount = 2
    ),
    ProductSales(
        productId = "PROD_d38a6bd6",
        productName = "Metformin",
        totalQuantity = 30,
        totalAmount = 960.0,
        remainingStock = 95,
        salesCount = 1
    ),
    ProductSales(
        productId = "PROD_d1fc4410",
        productName = "paracitamol",
        totalQuantity = 5,
        totalAmount = 25.0,
        remainingStock = 5,
        salesCount = 1
    ),
    ProductSales(
        productId = "PROD_0553ebba",
        productName = "Zinc Supplements",
        totalQuantity = 20,
        totalAmount = 400.0,
        remainingStock = 100,
        salesCount = 1
    )
)

val mockDashboardStats = DashboardStats(
    totalProducts = 5,
    lowStockProducts = 2,
    outOfStockProducts = 0,
    totalSales = 6410.0,
    todaySales = 425.0,
    salesGrowth = 12.5
)

val mockChartData = listOf(
    ChartData(date = "2025-07-10", sales = 800.0, quantity = 12),
    ChartData(date = "2025-07-11", sales = 1200.0, quantity = 18),
    ChartData(date = "2025-07-12", sales = 4385.0, quantity = 160),
    ChartData(date = "2025-07-13", sales = 950.0, quantity = 14),
    ChartData(date = "2025-07-14", sales = 1150.0, quantity = 16),
    ChartData(date = "2025-07-15", sales = 1350.0, quantity = 20),
    ChartData(date = "2025-07-16", sales = 1600.0, quantity = 24),
    ChartData(date = "2025-07-17", sales = 825.0, quantity = 25)
)




@Composable
fun ProductSalesCards(productSales: List<ProductSales>) {

    // Function to determine the stock status
    fun getStockStatus(remainingStock: Int): String {
        return when {
            remainingStock == 0 -> "out-of-stock"
            remainingStock <= 20 -> "low-stock"
            else -> "in-stock"
        }
    }

    // Function to determine the badge color
    fun getStatusColor(status: String): Color {
        return when (status) {
            "in-stock" ->Color.Green
            "low-stock" -> Color.Yellow
            "out-of-stock" -> Color.Red
            else -> Color.Gray
        }
    }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Product Sales",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        productSales.forEach { product ->
            val status = getStockStatus(product.remainingStock)
            val statusColor = getStatusColor(status)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .border(2.dp, statusColor, RoundedCornerShape(8.dp)),
                elevation = CardDefaults.elevatedCardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.productName,
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Badge(status = status)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCard(statValue = product.totalQuantity.toString(), label = "Sold")
                        StatCard(statValue = "$${product.totalAmount}", label = "Revenue")
                        StatCard(statValue = product.remainingStock.toString(), label = "Stock")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Additional Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Orders",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${product.salesCount} orders",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Average Sale",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$${String.format("%.2f",product.totalAmount / product.totalQuantity)} avg",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Badge(status: String) {
    Text(
        text = when (status) {
            "in-stock" -> "In Stock"
            "low-stock" -> "Low Stock"
            else -> "Out of Stock"
        },
        color = when (status) {
            "in-stock" -> Color.Green
            "low-stock" -> Color.Yellow
            else -> Color.Red
        },
        style = TextStyle(fontWeight = FontWeight.Bold)
    )
}

@Composable
fun StatCard(statValue: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth(0.3f)
            .padding(8.dp)
            .background(Color.Green, RoundedCornerShape(8.dp))
    ) {
        Text(
            text = statValue,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.Green
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
        )
    }
}






