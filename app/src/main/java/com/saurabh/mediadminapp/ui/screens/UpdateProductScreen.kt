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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayGradientBackdrop
import com.saurabh.mediadminapp.ui.screens.components.ClayOutlinedButton
import com.saurabh.mediadminapp.ui.screens.components.ClayPrimaryButton
import com.saurabh.mediadminapp.ui.screens.components.ClayTextField
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.utils.utilityFunctions.DismissKeyboardOnTapScreen
import com.saurabh.mediadminapp.utils.utilityFunctions.createImageUri
import com.saurabh.mediadminapp.utils.utilityFunctions.getUCropIntent
import com.saurabh.mediadminapp.utils.utilityFunctions.toMultipartBodyPart
import com.yalantis.ucrop.UCrop

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

    // ── Dialog & Camera States ──────────────────────────────────────────────
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }


    // 1. Crop Result Launcher (Yeh Composable ke andar rahega)
    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            if (resultUri != null) {
                newImageUri = resultUri // Cropped image yahan set ho jayegi
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            Toast.makeText(context, "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Gallery Launcher (Jab gallery se select ho, toh utility function call karo)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val cropIntent = getUCropIntent(context, uri) // 👈 Utility function ka use
            cropLauncher.launch(cropIntent)
        }
    }

// 3. Camera Launcher (Jab camera se click ho, toh utility function call karo)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            val cropIntent = getUCropIntent(context, tempCameraUri!!) // 👈 Utility function ka use
            cropLauncher.launch(cropIntent)
        }
    }

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
        Box(modifier = modifier.fillMaxSize().background(Color.Transparent)) {
            // ── Background gradient ───────────────────────────────
            ClayGradientBackdrop {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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

                        // ── Image preview / picker — hardware canvas shadow ──
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .drawBehind {
                                    val cr = 24.dp.toPx()
                                    drawIntoCanvas { canvas ->
                                        canvas.nativeCanvas.drawRoundRect(
                                            5.dp.toPx(), 7.dp.toPx(),
                                            size.width - 5.dp.toPx(), size.height + 5.dp.toPx(),
                                            cr, cr,
                                            android.graphics.Paint().apply {
                                                isAntiAlias = true
                                                color = android.graphics.Color.argb(65, 108, 99, 255)
                                                maskFilter = android.graphics.BlurMaskFilter(
                                                    18.dp.toPx(),
                                                    android.graphics.BlurMaskFilter.Blur.NORMAL
                                                )
                                            }
                                        )
                                    }
                                }
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

                        // Trigger dialog on click instead of direct gallery launcher
                        ClayOutlinedButton(
                            text = if (newImageUri == null && currentImageUrl.isNullOrEmpty())
                                "Select Product Image" else "Change Image",
                            onClick = {
                                showImageSourceDialog = true
                            },
                            leadingIcon = Icons.Default.AddCircle
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // ── Form Card — uses ClayCard (hardware shadow) ───────
                        ClayCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            Text(
                                text = "Edit Product Details",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ClayTextPrimary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            ClayTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = "Product Name",
                                leadingIcon = { Icon(Icons.Default.MedicalServices, contentDescription = null) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            ClayTextField(
                                value = price,
                                onValueChange = { price = it },
                                label = "Price (₹)",
                                leadingIcon = { Icon(Icons.Default.PriceChange, contentDescription = null) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            ClayTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = "Category",
                                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            ClayTextField(
                                value = stock,
                                onValueChange = { stock = it },
                                label = "Stock Quantity",
                                leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) }
                            )

                            Spacer(modifier = Modifier.height(20.dp))

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

    // ── Image Source Selection Dialog (Camera / Gallery) ───────────────────
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Choose Image Source") },
            text = {
                Column {
                    ClayOutlinedButton(
                        text = "Camera",
                        onClick = {
                            showImageSourceDialog = false
                            tempCameraUri = context.createImageUri()
                            cameraLauncher.launch(tempCameraUri!!)
                        },
                        leadingIcon = Icons.Default.CameraAlt,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    ClayOutlinedButton(
                        text = "Gallery",
                        onClick = {
                            showImageSourceDialog = false
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        leadingIcon = Icons.Default.PhotoLibrary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {}
        )
    }
}