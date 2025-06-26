package com.saurabh.mediadminapp.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UpdateProductScreen(productId : String, viewModel: MyViewModel, navController: NavController,modifier: Modifier = Modifier) {
    val response = viewModel.updateProductState.collectAsState()
    val productState by viewModel.getSpecificProductState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Fetch product details on first composition
    LaunchedEffect(productId) {
        viewModel.getSpecificProduct(productId)
    }

    // Local state for form fields, prefilled from productState
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    // Prefill fields when product is loaded
    LaunchedEffect(productState.success) {
        productState.success?.product?.let { product ->
            if (!isInitialized) {
                name = product.name
                price = product.price.toString()
                category = product.category
                stock = product.stock.toString()
                isInitialized = true
            }
        }
    }

    LaunchedEffect(response.value.success != null) {
        response.value.success?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            viewModel.clearUpdateProductState()
            navController.popBackStack() // Go back after update
            snackbarHostState.showSnackbar("Product updated successfully!")

        }
    }
    LaunchedEffect(response.value.error) {
        response.value.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "Update Product", modifier = Modifier.padding(bottom = 16.dp))
            HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
            if (productState.isLoading) {
                Text("Loading product details...")
            } else if (productState.error != null) {
                Text("Error loading product: ${'$'}{productState.error}")
            } else if (isInitialized) {
                // Editable fields using TextField
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                TextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                TextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                TextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val priceDouble = price.toDoubleOrNull()
                        val stockInt = stock.toIntOrNull()
                        if (name.isNotBlank() && priceDouble != null && category.isNotBlank() && stockInt != null) {
                            viewModel.updateProduct(
                                productId = productId,
                                name = name,
                                price = priceDouble,
                                category = category,
                                stock = stockInt
                            )
                        }
                    },
                    enabled = !response.value.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (response.value.isLoading) "Updating..." else "Update Product")
                }
                if (response.value.error != null) {
                    Text(text = response.value.error ?: "", modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}