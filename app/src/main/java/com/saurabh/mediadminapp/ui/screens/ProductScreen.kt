package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.ProductItem
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord
import java.text.NumberFormat
import java.util.Locale


@Composable
fun ProductScreen(viewModel: MyViewModel, navController: NavController) {
    // FIX (Cause 6): use 'by' delegation so Compose reads the unwrapped value,
    // enabling smarter recomposition skipping compared to .collectAsState()
    val productState by viewModel.getAllProduct.collectAsState()
    val currentState = navController.currentBackStackEntry
    LaunchedEffect(currentState) {
        val refresh = currentState?.savedStateHandle?.get<Boolean>("refresh_screen") == true
        if (refresh) {
            viewModel.getAllProduct(force = true)
            currentState.savedStateHandle.remove<Boolean>("refresh_screen")
        }
    }
    LaunchedEffect(key1 = Unit) {
        viewModel.getAllProduct()
    }

    Scaffold(
//       modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.AddProductRoutes.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { innerpadding ->

        when {
            productState.isLoading -> {
                Box(
                    modifier = Modifier
                        .padding(innerpadding)
                        .fillMaxSize()
                ) {
                    LoadingScreen(modifier = Modifier)
                }
            }
            productState.error != null -> {
                Box(
                    modifier = Modifier
                        .padding(innerpadding)
                        .fillMaxSize()
                ) {
                    Log.d("TAG", "ProductScreen: error :-> ${productState.error}")
                    ErrorScreen(
                        errorMessage = productState.error.toString(),
                    )
                }
            }
            productState.success != null -> {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ProductListScreen(
                        productState.success!!.products,
                        navController,
                        modifier = Modifier.background(Color(0xFFffffff)),
                        viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun ProductListScreen(
    products: List<ProductItem>,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: MyViewModel
) {
    var searchTerm by remember { mutableStateOf("") }
    var filterCategory by remember { mutableStateOf("all") }
    val categories = listOf("all", "tablet", "capsule", "liquid", "injection")

    val cardColors = listOf(
        Color(0xFFD3C4FA), // Lavender
        Color(0xFFACF68F), // Green
        Color(0xFFE9CF87), // Amber
        Color(0xFFFBE89E), // Yellow
        Color(0xFFFACDD4)  // Pink
    )

    // Filter products based on search and category
    val filteredProducts = remember(products, searchTerm, filterCategory) {
        products.filter { product ->
            val matchesSearch = product.name.contains(searchTerm, ignoreCase = true)
            val matchesCategory = filterCategory == "all" ||
                    product.category.lowercase() == filterCategory.lowercase()
            matchesSearch && matchesCategory
        }
    }

    val stats = remember(products) {
        mapOf(
            "total" to products.size,
            "inStock" to products.count { it.stock > 10 },
            "lowStock" to products.count { it.stock in 1..10 },
            "outOfStock" to products.count { it.stock == 0 }
        )
    }

    // FIX (Cause 5): wrapped in remember{} — Brush.horizontalGradient is not free;
    // without remember it creates a new Brush object on every recomposition.
    val medicalGradient = remember {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF0EA5E9), // sky-500
                Color(0xFF3B82F6)  // blue-500
            )
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            // stats summary
            Row(
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["total"].toString(),
                    label = "Total Items",
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["inStock"].toString(),
                    label = "In Stock",
                    valueColor = Color(0xFF10B981)
                )
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["lowStock"].toString(),
                    label = "Low Stock",
                    valueColor = Color(0xFF0EA5E9)
                )
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["outOfStock"].toString(),
                    label = "Out of Stock",
                    valueColor = Color(0xFFF59EBB)
                )
            }
        }
        item {
            // search bar
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
        item {
            // filter chips
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
            // FIX (Cause 5): was itemsIndexed(products) — incorrectly rendered the FULL list
            // even when a search/filter was active. Now correctly uses filteredProducts.
            itemsIndexed(filteredProducts) { index, productItem ->
                val bgColor = cardColors[index % cardColors.size]
                EachProductCard(productItem, navController, bgColor)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun previe() {
    val navController = rememberNavController()
    val products = ProductItem(
        Product_id = "PROD_d1fc4410",
        id = 5,
        name = "Vitamin D3 Tablets",
        price = 35.00,
        category = "Tablet",
        stock = 75
    )
    EachProductCard(products, navController, Color(0xFFD3C4FA))
}


@Composable
fun CategoryFilterButton(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    medicalGradient: Brush
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable(onClick = onClick)
            .background(
                brush = if (isSelected) medicalGradient else Brush.horizontalGradient(
                    listOf(Color.LightGray, Color.LightGray)
                ),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Text(
            text = category.replaceFirstChar { it.uppercase() },
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
fun EachProductCard(medicine: ProductItem, navController: NavController, cardBgColor: Color) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardBgColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageUrl = medicine.image_url
            if (!imageUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .background(Color.White),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", fontSize = 10.sp, color = Color.DarkGray)
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalScrollable1 {
                        Text(
                            text = medicine.name.capitalizeEachWord(),
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            maxLines = 1
                        )
                    }

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
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = medicine.category.capitalizeEachWord(),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "Stock: ${medicine.stock}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${NumberFormat.getInstance(Locale("en", "IN")).format(medicine.price)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedButton(
                        onClick = { navController.navigate(Routes.SpecificProductRoutes.invoke(medicine.Product_id)) },
                        modifier = Modifier.wrapContentSize(),
                        colors = ButtonDefaults.buttonColors(Color(0xFF7089F0)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "View",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Details", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
