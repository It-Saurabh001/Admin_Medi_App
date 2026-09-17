package com.saurabh.mediadminapp.ui.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.utils.utilityFunctions.DismissKeyboardOnTapScreen
import com.saurabh.mediadminapp.utils.utilityFunctions.toMultipartBodyPart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(viewModel: MyViewModel,navController: NavController,modifier: Modifier = Modifier) {
    val response = viewModel.addProductState.collectAsState()
    val context = LocalContext.current
    val name = remember { mutableStateOf("") }
    val price = remember { mutableStateOf("") }
    val category = remember { mutableStateOf("") }
    val stock = remember { mutableStateOf("") }



    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        imageUri = uri
    }

    LaunchedEffect(response.value.success) {
        response.value.success?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
            Log.d("TAG", "AddProductScreen: ${it.message}")
            viewModel.clearAddProductState()

            navController.previousBackStackEntry?.savedStateHandle?.set("refresh_screen",true)
            navController.popBackStack()
        }
    }

    LaunchedEffect(response.value.error) {
        response.value.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            Log.d("TAG", "AddProductScreen: ${it}")
        }
    }

    DismissKeyboardOnTapScreen {
        Scaffold { innerpadding->
            when{
                response.value.isLoading ->{
                    LoadingScreen()
                }
                response.value.error != null ->{
                    ErrorScreen(errorMessage = response.value.error.toString(),modifier = Modifier.padding(innerpadding))
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .padding(innerpadding)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        if (imageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUri),
                                contentDescription = "Product Image Preview",
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Button(onClick = {
                            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                            Text(if (imageUri == null) "Select Product Image" else "Change Image")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = name.value,
                            onValueChange = { name.value = it },
                            label = { Text(text = "Name") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = price.value,
                            onValueChange = { price.value = it },
                            label = { Text(text = "Price") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = category.value,
                            onValueChange = { category.value = it },
                            label = { Text(text = "Category") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = stock.value,
                            onValueChange = { stock.value = it },
                            label = { Text(text = "Stock") })

                        HorizontalDivider(modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp))

                        Button(
                            onClick = {
                                if (validateInput(name.value, price.value, category.value, stock.value)){
                                    val imagePart = imageUri?.toMultipartBodyPart(context, "image")
                                    viewModel.addProduct(
                                        name.value,
                                        price.value.toDouble(),
                                        category.value,
                                        stock.value.toInt(),
                                        imagePart
                                    )
                                }else{
                                    Toast.makeText(context, "Please fill all the fields correctly", Toast.LENGTH_LONG).show()
                                }
                            },
                            enabled = !response.value.isLoading,
                            modifier = Modifier.fillMaxWidth(0.5f).padding(16.dp)
                        ) {
                            Text(text = "Add Product")
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
