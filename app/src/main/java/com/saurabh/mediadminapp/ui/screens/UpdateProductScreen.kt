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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.saurabh.mediadminapp.ui.screens.components.ClayGradientBackdrop
import com.saurabh.mediadminapp.ui.screens.components.ClayOutlinedButton
import com.saurabh.mediadminapp.ui.screens.components.ClayPrimaryButton
import com.saurabh.mediadminapp.ui.screens.components.ClayTextField
import com.saurabh.mediadminapp.ui.theme.ClayCardBg
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.utils.utilityFunctions.DismissKeyboardOnTapScreen
import com.saurabh.mediadminapp.utils.utilityFunctions.toMultipartBodyPart

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UpdateProductScreen(
    productId: String,
    viewModel: MyViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val response = viewModel.updateProductState.collectAsState()
    val productState by viewModel.getSpecificProductState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(productId) {
        viewModel.getSpecificProduct(productId)
    }

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> newImageUri = uri }

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
            navController.popBackStack()
            snackbarHostState.showSnackbar("Product updated successfully!")
        }
    }

    LaunchedEffect(response.value.error) {
        response.value.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    DismissKeyboardOnTapScreen {
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            ClayGradientBackdrop {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ── AppBar on gradient ────────────────────────────────────
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
                            text = "Update Product",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (productState.isLoading) {
                        Text("Loading product details...", color = Color.White, modifier = Modifier.padding(32.dp))
                    } else if (!isInitialized) {
                        Text("Preparing form...", color = Color.White, modifier = Modifier.padding(32.dp))
                    } else {
                        val currentImageUrl = productState.success?.product?.image_url

                        // ── Image preview / picker ────────────────────────────
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .shadow(
                                    elevation = 20.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    ambientColor = ClayPrimary.copy(0.3f),
                                    spotColor = ClayPrimary.copy(0.3f)
                                )
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (newImageUri != null || !currentImageUrl.isNullOrEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = newImageUri ?: currentImageUrl),
                                    contentDescription = "Product Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Add Photo",
                                    tint = Color.White.copy(0.8f),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        ClayOutlinedButton(
                            text = if (newImageUri == null && currentImageUrl.isNullOrEmpty())
                                "Select Product Image" else "Change Image",
                            onClick = {
                                launcher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            leadingIcon = Icons.Default.AddCircle
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // ── Form Card ─────────────────────────────────────────
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
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Edit Product Details",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ClayTextPrimary
                            )

                            ClayTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = "Product Name",
                                leadingIcon = { Icon(Icons.Default.MedicalServices, contentDescription = null) }
                            )
                            ClayTextField(
                                value = price,
                                onValueChange = { price = it },
                                label = "Price (₹)",
                                leadingIcon = { Icon(Icons.Default.PriceChange, contentDescription = null) }
                            )
                            ClayTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = "Category",
                                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) }
                            )
                            ClayTextField(
                                value = stock,
                                onValueChange = { stock = it },
                                label = "Stock Quantity",
                                leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ClayPrimaryButton(
                                text = if (response.value.isLoading) "Updating..." else "Update Product",
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
                                isLoading = response.value.isLoading,
                                enabled = !response.value.isLoading,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (response.value.error != null) {
                                Text(
                                    text = response.value.error ?: "",
                                    modifier = Modifier.padding(top = 4.dp),
                                    color = Color.Red
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(36.dp))
                    }
                }
            }
        }
    }
}