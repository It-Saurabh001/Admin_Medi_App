package com.saurabh.mediadminapp.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.utils.utilityFunctions.createImageUri
import com.saurabh.mediadminapp.utils.utilityFunctions.getUCropIntent
import com.saurabh.mediadminapp.utils.utilityFunctions.toImagePart
import com.yalantis.ucrop.UCrop

@Composable
fun UpdateProductScreen(
    productId: String,
    viewModel: MyViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val response by viewModel.updateProductState.collectAsState()
    val productState by viewModel.getSpecificProductState.collectAsState()
    val context = LocalContext.current

    var name by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var initialized by rememberSaveable { mutableStateOf(false) }
    var newImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var showSource by rememberSaveable { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var priceError by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryError by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(productId) { viewModel.getSpecificProduct(productId) }

    val cropLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { UCrop.getOutput(it) }?.let { newImageUri = it }
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) cropLauncher.launch(getUCropIntent(context, uri))
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val u = cameraUri
        if (ok && u != null) cropLauncher.launch(getUCropIntent(context, u))
    }

    LaunchedEffect(productState.success) {
        productState.success?.let { p ->
            if (!initialized) {
                name = p.name; price = p.price.toString(); category = p.category; initialized = true
            }
        }
    }
    LaunchedEffect(response.success) {
        if (response.success != null) {
            viewModel.clearUpdateProductState()
            viewModel.getSpecificProduct(productId, force = true)
            navController.previousBackStackEntry?.savedStateHandle?.set("refresh_screen", true)
            navController.popBackStack()
        }
    }
    LaunchedEffect(response.error) {
        if (response.error != null) {
            nameError = response.appError.fieldError("name")
            priceError = response.appError.fieldError("price")
            categoryError = response.appError.fieldError("category")
        }
    }
    val formError = response.error.takeIf { nameError == null && priceError == null && categoryError == null }

    ProductFormScaffold(
        title = "Edit product", saveText = "Save",
        name = name, onName = { name = it; nameError = null }, nameError = nameError,
        price = price, onPrice = { price = it; priceError = null }, priceError = priceError,
        category = category, onCategory = { category = it; categoryError = null }, categoryError = categoryError,
        imageModel = newImageUri ?: productState.success?.imageUrl?.takeIf { it.isNotEmpty() },
        onPickImage = { showSource = true },
        isLoading = response.isLoading || (productState.isLoading && !initialized),
        formError = formError,
        onBack = { navController.popBackStack() },
        onSave = {
            nameError = if (name.isBlank()) "Name is required" else null
            priceError = if (price.toDoubleOrNull() == null) "Enter a valid price" else null
            if (nameError == null && priceError == null) {
                viewModel.updateProduct(productId, name.trim(), price.toDouble(), category.trim(), newImageUri?.toImagePart(context))
            }
        }
    )

    if (showSource) {
        AlertDialog(
            onDismissRequest = { showSource = false },
            title = { Text("Choose image source", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("Take a new photo or pick one from your gallery.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = {
                    showSource = false
                    cameraUri = context.createImageUri()
                    cameraUri?.let { cameraLauncher.launch(it) }
                }) { Text("Camera") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSource = false
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) { Text("Gallery") }
            }
        )
    }
}
