package com.saurabh.mediadminapp.ui.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.components.ClayErrorScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayGradientBackdrop
import com.saurabh.mediadminapp.ui.screens.components.ClayLoadingScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayOutlinedButton
import com.saurabh.mediadminapp.ui.screens.components.ClayPrimaryButton
import com.saurabh.mediadminapp.ui.screens.components.ClayTextField
import com.saurabh.mediadminapp.ui.theme.ClayBorder
import com.saurabh.mediadminapp.ui.theme.ClayCardBg
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClaySecondary
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary
import com.saurabh.mediadminapp.utils.utilityFunctions.DismissKeyboardOnTapScreen
import com.saurabh.mediadminapp.utils.utilityFunctions.toMultipartBodyPart

@Composable
fun AddProductScreen(viewModel: MyViewModel, navController: NavController, modifier: Modifier = Modifier) {
    val response = viewModel.addProductState.collectAsState()
    val context = LocalContext.current
    val name = remember { mutableStateOf("") }
    val price = remember { mutableStateOf("") }
    val category = remember { mutableStateOf("") }
    val stock = remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> imageUri = uri }

    LaunchedEffect(response.value.success) {
        response.value.success?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            Log.d("TAG", "AddProductScreen: ${it.message}")
            viewModel.clearAddProductState()
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh_screen", true)
            navController.popBackStack()
        }
    }

    LaunchedEffect(response.value.error) {
        response.value.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            Log.d("TAG", "AddProductScreen: $it")
        }
    }

    DismissKeyboardOnTapScreen {
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            when {
                response.value.isLoading -> {
                    ClayLoadingScreen(modifier = Modifier.padding(innerPadding))
                }
                response.value.error != null -> {
                    ClayErrorScreen(
                        errorMessage = response.value.error.toString(),
                        modifier = Modifier.padding(innerPadding),
                        onRetry = { viewModel.clearAddProductState() }
                    )
                }
                else -> {
                    ClayGradientBackdrop {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // ── Header bar on gradient ────────────────────────
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
                                    text = "Add New Product",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            // ── Image well / picker on gradient ───────────────
                            Spacer(modifier = Modifier.height(16.dp))
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
                                if (imageUri != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(imageUri),
                                        contentDescription = "Product Image Preview",
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
                                text = if (imageUri == null) "Select Product Image" else "Change Image",
                                onClick = {
                                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                leadingIcon = Icons.Default.AddCircle
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            // ── Form Card ─────────────────────────────────────
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
                                    text = "Product Details",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ClayTextPrimary
                                )

                                ClayTextField(
                                    value = name.value,
                                    onValueChange = { name.value = it },
                                    label = "Product Name",
                                    leadingIcon = { Icon(Icons.Default.MedicalServices, contentDescription = null, tint = ClayPrimary) }
                                )
                                ClayTextField(
                                    value = price.value,
                                    onValueChange = { price.value = it },
                                    label = "Price (₹)",
                                    leadingIcon = { Icon(Icons.Default.PriceChange, contentDescription = null, tint = ClayPrimary) }
                                )
                                ClayTextField(
                                    value = category.value,
                                    onValueChange = { category.value = it },
                                    label = "Category",
                                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = ClayPrimary) }
                                )
                                ClayTextField(
                                    value = stock.value,
                                    onValueChange = { stock.value = it },
                                    label = "Stock Quantity",
                                    leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null, tint = ClayPrimary) }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                ClayPrimaryButton(
                                    text = "Add Product",
                                    onClick = {
                                        if (validateInput(name.value, price.value, category.value, stock.value)) {
                                            val imagePart = imageUri?.toMultipartBodyPart(context, "image")
                                            viewModel.addProduct(
                                                name.value,
                                                price.value.toDouble(),
                                                category.value,
                                                stock.value.toInt(),
                                                imagePart
                                            )
                                        } else {
                                            Toast.makeText(context, "Please fill all the fields correctly", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    enabled = !response.value.isLoading,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun validateInput(name: String, price: String, category: String, stock: String): Boolean {
    return name.isNotBlank() &&
            price.isNotBlank() &&
            category.isNotBlank() &&
            stock.isNotBlank() &&
            price.toDoubleOrNull() != null &&
            stock.toIntOrNull() != null
}
