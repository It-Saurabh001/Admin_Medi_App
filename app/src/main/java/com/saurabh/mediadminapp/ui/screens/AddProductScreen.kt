package com.saurabh.mediadminapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.components.SectionHeader
import com.saurabh.mediadminapp.utils.utilityFunctions.DismissKeyboardOnTapScreen
import com.saurabh.mediadminapp.utils.utilityFunctions.toImagePart

/** Shared Add/Edit product form. Only fields the ViewModel can send: name, price, category, image. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProductFormScaffold(
    title: String,
    saveText: String,
    name: String, onName: (String) -> Unit, nameError: String?,
    price: String, onPrice: (String) -> Unit, priceError: String?,
    category: String, onCategory: (String) -> Unit, categoryError: String?,
    imageModel: Any?,
    onPickImage: () -> Unit,
    isLoading: Boolean,
    formError: String?,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
                Button(
                    onClick = onSave,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp)
                ) {
                    if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    else Text(saveText, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    ) { inner ->
        DismissKeyboardOnTapScreen {
            Column(
                Modifier.padding(inner).fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SectionHeader("Image", Modifier.padding(top = 8.dp))
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(120.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (imageModel != null) {
                                Image(rememberAsyncImagePainter(imageModel), "Product image", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else {
                                Icon(Icons.Outlined.AddAPhoto, contentDescription = null, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                    OutlinedButton(onClick = onPickImage, modifier = Modifier.padding(top = 8.dp).height(48.dp)) {
                        Text(if (imageModel == null) "Add image" else "Change image")
                    }
                }
                SectionHeader("Details")
                val fieldMod = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                OutlinedTextField(
                    value = name, onValueChange = onName, label = { Text("Product name") },
                    singleLine = true, isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), modifier = fieldMod
                )
                OutlinedTextField(
                    value = category, onValueChange = onCategory, label = { Text("Category") },
                    singleLine = true, isError = categoryError != null,
                    supportingText = categoryError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), modifier = fieldMod
                )
                SectionHeader("Pricing")
                OutlinedTextField(
                    value = price, onValueChange = onPrice, label = { Text("Selling price (₹)") },
                    singleLine = true, isError = priceError != null,
                    supportingText = priceError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done), modifier = fieldMod
                )
                formError?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = fieldMod)
                }
            }
        }
    }
}

@Composable
fun AddProductScreen(viewModel: MyViewModel, navController: NavController, modifier: Modifier = Modifier) {
    val response by viewModel.addProductState.collectAsState()
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var priceError by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryError by rememberSaveable { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) imageUri = uri }

    LaunchedEffect(response.success) {
        if (response.success != null) {
            viewModel.clearAddProductState()
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
        title = "Add product", saveText = "Save",
        name = name, onName = { name = it; nameError = null }, nameError = nameError,
        price = price, onPrice = { price = it; priceError = null }, priceError = priceError,
        category = category, onCategory = { category = it; categoryError = null }, categoryError = categoryError,
        imageModel = imageUri,
        onPickImage = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        isLoading = response.isLoading, formError = formError,
        onBack = { navController.popBackStack() },
        onSave = {
            nameError = if (name.isBlank()) "Name is required" else null
            priceError = if (price.toDoubleOrNull() == null) "Enter a valid price" else null
            if (nameError == null && priceError == null) {
                viewModel.addProduct(name.trim(), price.toDouble(), category.trim(), imageUri?.toImagePart(context))
            }
        }
    )
}
