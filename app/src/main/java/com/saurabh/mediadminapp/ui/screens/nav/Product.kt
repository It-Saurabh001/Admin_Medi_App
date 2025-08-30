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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.*

// Data classes
data class Product(
    val Product_id: String,
    val id: String,
    val name: String,
    val price: Double,
    val category: String,
    val stock: Int
)

// Mock data
val mockProducts = listOf(
    Product(
        Product_id ="PROD_29c64715" ,
        id = "1",
        name = "Paracetamol 500mg",
        price = 25.50,
        category = "Tablet",
        stock = 150
    ),
    Product(
        Product_id="PROD_d1fc4410",
        id = "2",
        name = "Amoxicillin Capsules",
        price = 45.00,
        category = "Capsule",
        stock = 8
    ),
    Product(
        Product_id="PROD_d1fc4410",
        id = "3",
        name = "Cough Syrup",
        price = 85.00,
        category = "Liquid",
        stock = 25
    ),
    Product(
        Product_id="PROD_d1fc4410",
        id = "4",
        name = "Insulin Injection",
        price = 150.00,
        category = "Injection",
        stock = 0
    ),
    Product(
        Product_id="PROD_d1fc4410",
        id = "5",
        name = "Vitamin D3 Tablets",
        price = 35.00,
        category = "Tablet",
        stock = 75
    )
)
@Preview(showBackground = true)
@Composable
fun ProductsScreen(
    onViewProduct: (String) -> Unit = {},
    onAddToCart: (String) -> Unit = {}
) {
    var products by remember { mutableStateOf(mockProducts) }
    var searchTerm by remember { mutableStateOf("") }
    var filterCategory by remember { mutableStateOf("all") }

    val categories = listOf("all", "tablet", "capsule", "liquid", "injection")

    // Filter products based on search and category
    val filteredProducts = remember(products, searchTerm, filterCategory) {
        products.filter { product ->
            val matchesSearch = product.name.contains(searchTerm, ignoreCase = true)
            val matchesCategory = filterCategory == "all" ||
                    product.category.lowercase() == filterCategory.lowercase()

            matchesSearch && matchesCategory
        }
    }

    // Calculate stats
    val stats = remember(products) {
        mapOf(
            "total" to products.size,
            "inStock" to products.count { it.stock > 10 },
            "lowStock" to products.count { it.stock in 1..10 },
            "outOfStock" to products.count { it.stock == 0 }
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
                // Total Items
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["total"].toString(),
                    label = "Total Items",
                    valueColor = MaterialTheme.colorScheme.onSurface
                )

                // In Stock
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["inStock"].toString(),
                    label = "In Stock",
                    valueColor = MaterialTheme.colorScheme.primary
                )

                // Low Stock
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["lowStock"].toString(),
                    label = "Low Stock",
                    valueColor = Color(0xFFD97706) // yellow-600
                )

                // Out of Stock
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["outOfStock"].toString(),
                    label = "Out of Stock",
                    valueColor = MaterialTheme.colorScheme.error
                )
            }
        }

        // Search
        item {
            OutlinedTextField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                placeholder = { Text("Search medicines...") },
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

        // Category Filter
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(categories) { category ->
                    CategoryFilterButton(
                        category = category,
                        isSelected = filterCategory == category,
                        onClick = { filterCategory = category },
                        medicalGradient = medicalGradient
                    )
                }
            }
        }

        // Add New Product Button
        item {
            Button(
                onClick = { /* Handle add new product */ },
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
                Text("Add New Product")
            }
        }

        // Products Grid/List
        if (filteredProducts.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "No products",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No products found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredProducts) { product ->
                MedicineCard(
                    medicine = product,
                    onView = { onViewProduct(product.id) },
                    onAddToCart = { onAddToCart(product.id) }
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
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.Center
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
fun CategoryFilterButton(
    category: String,
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

    val icon = when (category) {
        "all" -> Icons.Default.List
        else -> Icons.Default.Add // Pill icon alternative
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
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = category.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                },
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
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = category.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                },
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun MedicineCard(
    medicine: Product,
    onView: () -> Unit,
    onAddToCart: () -> Unit
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
                    text = medicine.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )

                // Stock Status Badge
                Surface(
                    color = when {
                        medicine.stock == 0 -> MaterialTheme.colorScheme.error
                        medicine.stock <= 10 -> Color(0xFFF59E0B) // yellow-500
                        else -> Color(0xFF10B981) // green-500
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when {
                            medicine.stock == 0 -> "Out of Stock"
                            medicine.stock <= 10 -> "Low Stock"
                            else -> "In Stock"
                        },
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Category and Stock Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Badge
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = medicine.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "Stock: ${medicine.stock}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Price
            Text(
                text = "₹${NumberFormat.getInstance(Locale("en", "IN")).format(medicine.price)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
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
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "View",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View")
                }
                    // user mai hog
                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.weight(1f),
                    enabled = medicine.stock > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (medicine.stock > 0) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Add to Cart",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (medicine.stock > 0) "Add to Cart" else "Out of Stock",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}