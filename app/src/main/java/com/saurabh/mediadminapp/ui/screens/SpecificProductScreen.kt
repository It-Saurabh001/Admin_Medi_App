package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.ProductItem
import com.saurabh.mediadminapp.ui.screens.components.ClayDangerButton
import com.saurabh.mediadminapp.ui.screens.components.ClayErrorScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayGradientBackdrop
import com.saurabh.mediadminapp.ui.screens.components.ClayInfoRow
import com.saurabh.mediadminapp.ui.screens.components.ClayLoadingScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayPrimaryButton
import com.saurabh.mediadminapp.ui.screens.components.ClayStatusBadge
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.ui.theme.ClayBadgeInStock
import com.saurabh.mediadminapp.ui.theme.ClayBadgeLowStock
import com.saurabh.mediadminapp.ui.theme.ClayBadgeOutStock
import com.saurabh.mediadminapp.ui.theme.ClayBorder
import com.saurabh.mediadminapp.ui.theme.ClayCardBg
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SpecificProductScreen(productId: String, viewModel: MyViewModel, navController: NavController) {
    val productstate = viewModel.getSpecificProductState.collectAsState()
    val deleteProductResponse = viewModel.deleteProductState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(productId) {
        viewModel.clearGetSpecificProductState()
        viewModel.getSpecificProduct(productId)
    }

    LaunchedEffect(deleteProductResponse.value.success != null) {
        deleteProductResponse.value.success?.let {
            Toast.makeText(context, "Product Deleted Successfully", Toast.LENGTH_SHORT).show()
            Log.d("DeleteProductSuccess", deleteProductResponse.value.success.toString())
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh_screen", true)
            navController.popBackStack()
        }
    }

    LaunchedEffect(deleteProductResponse.value.error != null) {
        deleteProductResponse.value.error?.let {
            Toast.makeText(context, "Error deleting Product", Toast.LENGTH_SHORT).show()
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh_screen", true)
            Log.e("DeleteProductError", deleteProductResponse.value.error.toString())
        }
    }

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        when {
            productstate.value.isLoading -> {
                ClayLoadingScreen(modifier = Modifier.padding(innerPadding))
            }
            productstate.value.error != null -> {
                ClayErrorScreen(
                    errorMessage = productstate.value.error.toString(),
                    modifier = Modifier.padding(innerPadding)
                )
            }
            productstate.value.success != null -> {
                val productItem = productstate.value.success?.product
                if (productItem != null) {
                    ClayGradientBackdrop {
                        EachProduct(
                            productItem = productItem,
                            navController = navController,
                            onDeleteClick = {
                                viewModel.deleteProduct(productId)
                                Toast.makeText(context, "Deleting Product...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Product not found or has been deleted", fontSize = 18.sp, color = ClayTextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun EachProduct(
    productItem: ProductItem,
    navController: NavController,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inrFormat = NumberFormat.getInstance(
        Locale.Builder().setLanguage("en").setRegion("IN").build()
    )
    val stockBadgeColor = when {
        productItem.stock == 0 -> ClayBadgeOutStock
        productItem.stock <= 10 -> ClayBadgeLowStock
        else -> ClayBadgeInStock
    }
    val stockBadgeText = when {
        productItem.stock == 0 -> "Out of Stock"
        productItem.stock <= 10 -> "Low Stock"
        else -> "In Stock"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── AppBar on gradient ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Product Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Product Image Hero ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(200.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = ClayPrimary.copy(alpha = 0.3f),
                    spotColor = ClayPrimary.copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.18f))
                .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            val imageUrl = productItem.image_url
            if (!imageUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "Product Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "No Image",
                    tint = Color.White.copy(0.6f),
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Product name + stock badge on gradient
        Text(
            text = productItem.name.capitalizeEachWord(),
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(6.dp))
        ClayStatusBadge(text = stockBadgeText, color = Color.White)

        Spacer(modifier = Modifier.height(28.dp))

        // ── Details Card ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = ClayPrimary.copy(0.18f),
                    spotColor = ClayPrimary.copy(0.22f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(ClayCardBg)
                .border(1.5.dp, Color.White, RoundedCornerShape(28.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Product Information",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ClayTextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            ClayInfoRow(label = "Product ID", value = productItem.Product_id)
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = ClayBorder.copy(0.4f))
            ClayInfoRow(label = "Name", value = productItem.name.capitalizeEachWord())
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = ClayBorder.copy(0.4f))
            ClayInfoRow(label = "Category", value = productItem.category.capitalizeEachWord())
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = ClayBorder.copy(0.4f))
            ClayInfoRow(label = "Price", value = "₹${inrFormat.format(productItem.price)}")
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = ClayBorder.copy(0.4f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClayInfoRow(label = "Stock", value = "${productItem.stock} units", modifier = Modifier.weight(1f))
                ClayStatusBadge(text = stockBadgeText, color = stockBadgeColor)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Action Buttons ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ClayPrimaryButton(
                text = "Update Product Details",
                onClick = { navController.navigate(Routes.UpdateProductRoutes.invoke(productItem.Product_id)) },
                modifier = Modifier.fillMaxWidth()
            )
            ClayDangerButton(
                text = "Delete Product",
                onClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}