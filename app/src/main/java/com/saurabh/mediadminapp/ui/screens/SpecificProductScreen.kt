package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.ProductItem
import com.saurabh.mediadminapp.ui.screens.nav.UpdateProductRoutes
import com.saurabh.mediadminapp.ui.screens.nav.UserSettingsRoutes

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
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh_screen",true)
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

    Scaffold { innerpadding ->
        when {
            productstate.value.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            productstate.value.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerpadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error loading Product data", fontSize = 18.sp)
                        Text(productstate.value.error.toString(), fontSize = 14.sp)
                    }
                }
            }

            productstate.value.success != null -> {
                val productItem = productstate.value.success?.product
                if (productItem != null) {
                    EachProduct(productItem, navController, onDeleteClick = {
                        viewModel.deleteProduct(productId)
                        Toast.makeText(context, "Deleting Product...", Toast.LENGTH_SHORT).show()
                    })
                } else {
                    // Handle case when user is not found
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Product not found or has been deleted", fontSize = 18.sp)
                    }
                }
            }
        }

    }


}

@Composable
fun EachProduct(productItem: ProductItem, navController: NavController,onDeleteClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {


        Text(
            text = "Id: ${productItem.Product_id}",
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Name: ${productItem.name}",
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Category: ${productItem.category}",
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Price: ${productItem.price}",
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Stock: ${productItem.stock}",
            style = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        // add space using Spacer of 13.dp
        Spacer(modifier = Modifier.height(15.dp))
        //add button to update product details
        Button(
            onClick = {navController.navigate(UpdateProductRoutes.invoke(productItem.Product_id))
            },

            modifier = Modifier.fillMaxWidth(0.7f).padding(16.dp)
        ) {
            Text(text = "Update ProductDetails")
        }
        Spacer(modifier = Modifier.height(15.dp))
        Button(
            onClick = {onDeleteClick()},

            modifier = Modifier.fillMaxWidth(0.7f).padding(16.dp)
        ) {
            Text(text = "Delete Product")
        }
    }
}