package com.saurabh.mediadminapp.ui.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.utils.utilityFunctions.DismissKeyboardOnTapScreen
import com.saurabh.mediadminapp.utils.utilityFunctions.toMultipartBodyPart
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

    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        newImageUri = uri
    }

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
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh_screen", true)
            navController.popBackStack() // Go back after update
            snackbarHostState.showSnackbar("Product updated successfully!")

        }
    }
    LaunchedEffect(response.value.error) {
        response.value.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    DismissKeyboardOnTapScreen {
        Scaffold { innerPadding ->
            Column(
                modifier = modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Text(text = "Update Product", modifier = Modifier.padding(bottom = 16.dp))
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
                if (productState.isLoading) {
                    Text("Loading product details...")
                } else if (productState.error != null) {
                    Text("Error loading product: ${productState.error}")
                } else if (isInitialized) {
                    val currentImageUrl = productState.success?.product?.image_url
                    if (newImageUri != null || !currentImageUrl.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = newImageUri ?: currentImageUrl),
                                contentDescription = "Product Image",
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = {
                            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                            Text(if (newImageUri == null && currentImageUrl.isNullOrEmpty()) "Select Product Image" else "Change Image")
                        }
                    }

                    // Editable fields using TextField
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
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
                                val imagePart = newImageUri?.toMultipartBodyPart(context, "image")
                                viewModel.updateProduct(
                                    productId = productId,
                                    name = name,
                                    price = priceDouble,
                                    category = category,
                                    stock = stockInt,
                                    image = imagePart
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

}