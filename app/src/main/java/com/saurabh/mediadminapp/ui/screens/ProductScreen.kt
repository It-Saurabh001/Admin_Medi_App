package com.saurabh.mediadminapp.ui.screens

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory2
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.ProductItem
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayDetailButton
import com.saurabh.mediadminapp.ui.screens.components.ClayEmptyState
import com.saurabh.mediadminapp.ui.screens.components.ClayErrorScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayFilterChip
import com.saurabh.mediadminapp.ui.screens.components.ClayGradientBackdrop
import com.saurabh.mediadminapp.ui.screens.components.ClayLoadingScreen
import com.saurabh.mediadminapp.ui.screens.components.ClaySearchField
import com.saurabh.mediadminapp.ui.screens.components.ClayStatusBadge
import com.saurabh.mediadminapp.ui.screens.components.DonutChart
import com.saurabh.mediadminapp.ui.screens.components.buildProductStatsSegments
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.ui.theme.ClayBadgeInStock
import com.saurabh.mediadminapp.ui.theme.ClayBadgeLowStock
import com.saurabh.mediadminapp.ui.theme.ClayBadgeOutStock
import com.saurabh.mediadminapp.ui.theme.ClayFieldBg
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayProductGradient
import com.saurabh.mediadminapp.ui.theme.ClaySecondary
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary
import com.saurabh.mediadminapp.utils.cardColors
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductScreen(viewModel: MyViewModel, navController: NavController) {
    val productState = viewModel.getAllProduct.collectAsState().value

    LaunchedEffect(key1 = Unit) {
        viewModel.getAllProduct()
    }
    Box(modifier = Modifier.fillMaxSize()){
        ClayGradientBackdrop(gradient = ClayProductGradient) {
            when {
                productState.isLoading -> {
                    ClayLoadingScreen(modifier = Modifier)
                }
                productState.error != null -> {
                    ClayErrorScreen(
                        errorMessage = productState.error,
                        modifier = Modifier,
                        onRetry = { viewModel.getAllProduct() }
                    )
                }
                productState.success != null -> {
                    ProductCatalogShowcase(
                        products = productState.success.products,
                        navController = navController,
                        modifier = Modifier
                    )
                }
            }
        }
    }


}

@Composable
fun ProductCatalogShowcase(
    products: List<ProductItem>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var searchTerm by remember { mutableStateOf("") }
    var filterCategory by remember { mutableStateOf("all") }
    val categories = listOf("all", "tablet", "capsule", "liquid", "injection")

    val filteredProducts = remember(products, searchTerm, filterCategory) {
        products.filter { product ->
            val matchesSearch = product.name.contains(searchTerm, ignoreCase = true) ||
                    product.category.contains(searchTerm, ignoreCase = true)
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

    val cornerRadius = 28.dp
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 100.dp)
    ) {
        // ── 1. RECESSED SEARCH FIELD ─────────────────────────────────────
        item {
            ClaySearchField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                placeholder = "Search medicine catalog...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = ClayPrimary
                    )
                }
            )
        }

        // ── 2. FLOATING QUICK-FILTER DOCK ────────────────────────────────
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
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
        // ── 3. ASYMMETRIC HERO SHOWCASE TILE ──────────────────────────────
        item {
            CatalogCircularStatsCard(stats = stats,modifier = Modifier)
        }


        // ── 4. TWO-TIER SCANNABLE PRODUCT SHOWCASE CARDS ─────────────────
        if (filteredProducts.isEmpty()) {
            item {
                ClayEmptyState(
                    message = "No products found",
                    subtitle = "Try adjusting your search query or category filter",
                    emoji = "💊"
                )
            }
        } else {
            itemsIndexed(filteredProducts, key = { _, item -> item.Product_id }) { index, product ->
                val bgTint = cardColors[index % cardColors.size]
                AsymmetricProductCard(
                    product = product,
                    bgColor = bgTint,
                    onDetailsClick = {
                        navController.navigate(Routes.SpecificProductRoutes.invoke(product.Product_id))
                    }
                )
            }
        }
    }
}

@Composable
private fun StockIndicatorChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .border(0.5.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Re-architected Two-Tier Overview Product Card:
 * Pure scannable hierarchy: Image Inset Tile, Title, Category Badge, Floating Stock Chip,
 * Split Price Deck, and prominent high-contrast spring-reactive "Details" button.
 */
@Composable
fun AsymmetricProductCard(
    product: ProductItem,
    bgColor: Color,
    onDetailsClick: () -> Unit
) {
    val stockColor = when {
        product.stock == 0 -> ClayBadgeOutStock
        product.stock <= 10 -> ClayBadgeLowStock
        else -> ClayBadgeInStock
    }
    val stockText = when {
        product.stock == 0 -> "Out of Stock"
        product.stock <= 10 -> "Low Stock (${product.stock})"
        else -> "Stock: ${product.stock}"
    }

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        bgColor = bgColor
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Asymmetric Image Tile with Recessed Clay Inset
            val imageUrl = product.image_url
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ClayFieldBg)
                    .border(1.5.dp, Color.White, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = ClayPrimary.copy(alpha = 0.6f),
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Primary Content Information Deck
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Identity & Status Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name.capitalizeEachWord(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    ClayStatusBadge(
                        text = stockText,
                        color = stockColor
                    )
                }

                // Category Tag
                Text(
                    text = product.category.capitalizeEachWord(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ClayTextSecondary
                )

                // Split Pricing Deck + Prominent "Details" Trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PRICE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ClayTextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "₹${NumberFormat.getInstance(Locale.Builder().setLanguage("en").setRegion("IN").build()).format(product.price)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ClayPrimary
                        )
                    }

                    ClayDetailButton(
                        text = "Details",
                        onClick = onDetailsClick
                    )
                }
            }
        }
    }
}



@Composable
fun CatalogCircularStatsCard(
    stats: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val cornerRadius = 32.dp
    val totalItems = stats["total"] ?: 0
    val inStock = stats["inStock"] ?: 0
    val lowStock = stats["lowStock"] ?: 0
    val outOfStock = stats["outOfStock"] ?: 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            // 1. Hardware Canvas Blur Shadow (Tactile depth)
            .drawBehind {
                val cr = cornerRadius.toPx()
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawRoundRect(
                        4.dp.toPx(), 8.dp.toPx(),
                        size.width - 4.dp.toPx(), size.height + 8.dp.toPx(),
                        cr, cr,
                        android.graphics.Paint().apply {
                            isAntiAlias = true
                            color = android.graphics.Color.argb(60, 108, 99, 255) // ClayPrimary tinted shadow
                            maskFilter = android.graphics.BlurMaskFilter(
                                20.dp.toPx(),
                                android.graphics.BlurMaskFilter.Blur.NORMAL
                            )
                        }
                    )
                }
            }
            // 2. Soft Pillowy Surface Gradient
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFF4F7FD),
                        Color(0xFFE9EEF9),
                        Color(0xFFDFE6F5)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            // 3. Multi-Pass Pillow Lighting & Specular Highlights
            .drawWithContent {
                drawContent()
                val cr = CornerRadius(cornerRadius.toPx())
                val rimWidth = 1.8.dp.toPx()

                // Top Specular Highlight
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        0.00f to Color.White.copy(alpha = 0.90f),
                        0.25f to Color.White.copy(alpha = 0.25f),
                        0.50f to Color.White.copy(alpha = 0.00f)
                    ),
                    size = size,
                    cornerRadius = cr
                )
                // Bottom Contact Shadow
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        0.50f to Color.Black.copy(alpha = 0.00f),
                        0.80f to Color.Black.copy(alpha = 0.04f),
                        1.00f to Color.Black.copy(alpha = 0.18f)
                    ),
                    size = size,
                    cornerRadius = cr
                )
                // Top Rim Highlight Ring
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        0.00f to Color.White.copy(alpha = 1.00f),
                        0.35f to Color.White.copy(alpha = 0.30f),
                        0.70f to Color.White.copy(alpha = 0.00f)
                    ),
                    size = size,
                    cornerRadius = cr,
                    style = Stroke(width = rimWidth)
                )
            }
            .padding(22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header: Icon + Title ──────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ClayPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = ClayPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Catalog Inventory Analytics",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ClayTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Hero Central Circular Donut Chart ─────────────────────────────
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                DonutChart(
                    segments = buildProductStatsSegments(stats),
                    centerLabel = "$totalItems",
                    centerSubLabel = "Total Items",
                    chartSize = 150.dp
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ── Symmetrical Distribution Breakdown Strip (3-Column Grid) ──────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StockDistributionPill(
                    label = "In Stock",
                    count = "$inStock",
                    color = ClayBadgeInStock,
                    modifier = Modifier.weight(1f)
                )
                StockDistributionPill(
                    label = "Low Stock",
                    count = "$lowStock",
                    color = ClayBadgeLowStock,
                    modifier = Modifier.weight(1f)
                )
                StockDistributionPill(
                    label = "Out Stock",
                    count = "$outOfStock",
                    color = ClayBadgeOutStock,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


@Composable
fun StockDistributionPill(
    label: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = ClayTextSecondary
            )
        }
    }
}