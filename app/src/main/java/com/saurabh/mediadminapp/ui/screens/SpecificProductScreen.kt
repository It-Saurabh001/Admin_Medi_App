package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.ProductItem
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayDangerButton
import com.saurabh.mediadminapp.ui.screens.components.ClayErrorScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayInfoRow
import com.saurabh.mediadminapp.ui.screens.components.ClayLoadingScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayPrimaryButton
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.ui.theme.ClayScreenBg
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary

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

    Scaffold(modifier = Modifier.background(ClayScreenBg)) { innerpadding ->
        when {
            productstate.value.isLoading -> {
                ClayLoadingScreen(modifier = Modifier.padding(innerpadding))
            }

            productstate.value.error != null -> {
                ClayErrorScreen(
                    errorMessage = productstate.value.error.toString(),
                    modifier = Modifier.padding(innerpadding)
                )
            }

            productstate.value.success != null -> {
                val productItem = productstate.value.success?.product
                if (productItem != null) {
                    EachProduct(
                        productItem = productItem,
                        navController = navController,
                        onDeleteClick = {
                            viewModel.deleteProduct(productId)
                            Toast.makeText(context, "Deleting Product...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.padding(innerpadding)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerpadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Product not found or has been deleted", 
                            fontSize = 18.sp,
                            color = ClayTextPrimary
                        )
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        val imageUrl = productItem.image_url
        if (!imageUrl.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = "Product Image",
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(3.dp, Color(0xFFD0C8FF), RoundedCornerShape(16.dp))
                    .background(Color.White),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        ClayCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            ClayInfoRow(label = "Product ID", value = productItem.Product_id)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFD0C8FF))
            
            ClayInfoRow(label = "Name", value = productItem.name)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFD0C8FF))
            
            ClayInfoRow(label = "Category", value = productItem.category)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFD0C8FF))
            
            ClayInfoRow(label = "Price", value = "₹${productItem.price}")
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFD0C8FF))
            
            ClayInfoRow(label = "Stock", value = productItem.stock.toString())
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        ClayPrimaryButton(
            text = "Update Product Details",
            onClick = { navController.navigate(Routes.UpdateProductRoutes.invoke(productItem.Product_id)) },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ClayDangerButton(
            text = "Delete Product",
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}