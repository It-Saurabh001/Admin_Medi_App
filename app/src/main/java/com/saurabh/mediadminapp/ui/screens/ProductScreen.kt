package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.ProductItem
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayEmptyState
import com.saurabh.mediadminapp.ui.screens.components.ClayErrorScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayFilterChip
import com.saurabh.mediadminapp.ui.screens.components.ClayLoadingScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayOutlinedButton
import com.saurabh.mediadminapp.ui.screens.components.ClaySearchField
import com.saurabh.mediadminapp.ui.screens.components.ClayStatCard
import com.saurabh.mediadminapp.ui.screens.components.ClayStatusBadge
import com.saurabh.mediadminapp.ui.screens.components.DonutChart
import com.saurabh.mediadminapp.ui.screens.components.buildProductStatsSegments
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.ui.theme.ClayBadgeInStock
import com.saurabh.mediadminapp.ui.theme.ClayBadgeLowStock
import com.saurabh.mediadminapp.ui.theme.ClayBadgeOutStock
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayProductGradient
import com.saurabh.mediadminapp.ui.theme.ClayScreenBg
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductScreen(viewModel: MyViewModel, navController: NavController) {
    val productState = viewModel.getAllProduct.collectAsState().value

    LaunchedEffect(key1 = Unit) {
        viewModel.getAllProduct()
    }
    
    Scaffold(modifier = Modifier.background(ClayScreenBg)) { innerpadding ->
        when {
            productState.isLoading -> {
                ClayLoadingScreen(modifier = Modifier.padding(innerpadding))
            }
            productState.error != null -> {
                ClayErrorScreen(
                    errorMessage = productState.error,
                    modifier = Modifier.padding(innerpadding)
                )
            }
            productState.success != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ClayScreenBg)
                ) {
                    ProductListScreen(
                        products = productState.success.products,
                        navController = navController,
                        modifier = Modifier.padding(innerpadding),
                        viewModel = viewModel
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

    val filteredProducts = remember(products, searchTerm, filterCategory) {
        products.filter { product ->
            val matchesSearch = product.name.contains(searchTerm, ignoreCase = true)
            val matchesCategory = filterCategory == "all" ||
                    product.category.equals(filterCategory, ignoreCase = true)
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

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        item {
            ClayCard(modifier = Modifier.fillMaxWidth()) {
                DonutChart(
                    segments = buildProductStatsSegments(stats),
                    centerLabel = "${stats["total"]}",
                    centerSubLabel = "Total Stock",
                    chartSize = 160.dp
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClayStatCard(
                    modifier = Modifier.weight(1f),
                    value = stats["total"].toString(),
                    label = "Total Items",
                    accentColor = ClayTextPrimary
                )
                ClayStatCard(
                    modifier = Modifier.weight(1f),
                    value = stats["inStock"].toString(),
                    label = "In Stock",
                    accentColor = ClayBadgeInStock
                )
                ClayStatCard(
                    modifier = Modifier.weight(1f),
                    value = stats["lowStock"].toString(),
                    label = "Low Stock",
                    accentColor = ClayBadgeLowStock
                )
                ClayStatCard(
                    modifier = Modifier.weight(1f),
                    value = stats["outOfStock"].toString(),
                    label = "Out of Stock",
                    accentColor = ClayBadgeOutStock
                )
            }
        }
        item {
            ClaySearchField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                placeholder = "Search medicines...",
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
            )
        }
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(categories) { category ->
                    ClayFilterChip(
                        label = category.replaceFirstChar { it.uppercase() },
                        selected = filterCategory == category,
                        onClick = { filterCategory = category },
                        gradient = ClayProductGradient
                    )
                }
            }
        }
        
        if (filteredProducts.isEmpty()) {
            item {
                ClayEmptyState(
                    message = "No products found",
                    emoji = "💊"
                )
            }
        } else {
            itemsIndexed(filteredProducts) { index, productItem ->
                val bgColor = cardColors[index % cardColors.size]
                EachProductCard(productItem, navController, bgColor)
            }
        }
    }
}

@Composable
fun EachProductCard(medicine: ProductItem, navController: NavController, cardBgColor: Color) {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        bgColor = cardBgColor
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageUrl = medicine.image_url
            if (!imageUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", fontSize = 10.sp, color = ClayTextSecondary)
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalScrollableText(
                        text = medicine.name.capitalizeEachWord(),
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ClayTextPrimary
                        ),
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    ClayStatusBadge(
                        text = when {
                            medicine.stock == 0 -> "Out of Stock"
                            medicine.stock <= 10 -> "Low Stock"
                            else -> "In Stock"
                        },
                        color = when {
                            medicine.stock == 0 -> ClayBadgeOutStock
                            medicine.stock <= 10 -> ClayBadgeLowStock
                            else -> ClayBadgeInStock
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClayStatusBadge(
                        text = medicine.category.capitalizeEachWord(),
                        color = ClayTextPrimary
                    )

                    Text(
                        text = "Stock: ${medicine.stock}",
                        fontSize = 13.sp,
                        color = ClayTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${NumberFormat.getInstance(Locale.Builder().setLanguage("en").setRegion("IN").build()).format(medicine.price)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ClayPrimary
                    )

                    ClayOutlinedButton(
                        text = "Details",
                        onClick = { navController.navigate(Routes.SpecificProductRoutes.invoke(medicine.Product_id)) },
                        leadingIcon = Icons.Default.FavoriteBorder,
                        accentColor = Color(0xFF7089F0) // Matching original button color
                    )
                }
            }
        }
    }
}
