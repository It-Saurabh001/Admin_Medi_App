package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(viewModel: MyViewModel,navController: NavController,modifier: Modifier = Modifier) {
    val response = viewModel.addProductState.collectAsState()
    val context = LocalContext.current
    val name = remember { mutableStateOf("") }
    val price = remember { mutableStateOf("") }
    val category = remember { mutableStateOf("") }
    val stock = remember { mutableStateOf("") }



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

    Scaffold { innerpadding->
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 1.dp),
            thickness = 1.dp
        )
        Column(
            modifier = Modifier
                .padding(innerpadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text(text = "Name") })
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = price.value,
                onValueChange = { price.value = it },
                label = { Text(text = "price") })
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = category.value,
                onValueChange = { category.value = it },
                label = { Text(text = "category") })
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = stock.value,
                onValueChange = { stock.value = it },
                label = { Text(text = "stock") })


            HorizontalDivider(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp))

            Button(
                onClick = {
                    if (validateInput(name.value, price.value, category.value, stock.value)){
                        viewModel.addProduct(
                            name.value,
                            price.value.toDouble(),
                            category.value,
                            stock.value.toInt()
                        )
                    }else{
                        Toast.makeText(context, "Please fill all the fields", Toast.LENGTH_LONG).show()
                    }
                    Log.d("TAG", "AddProductScreen: ${response.value.success?.message}")
                },
                enabled = !response.value.isLoading,
                modifier = Modifier.fillMaxWidth(0.5f).padding(16.dp)
            ) {
                Text(text = "Add Product")
            }
            if(response.value.isLoading){
                LoadingScreen()
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

@Preview(showBackground = true)
@Composable
fun AddProductScreenSimplePreview() {
    Scaffold { innerpadding->
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 1.dp),
            thickness = 1.dp
        )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerpadding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text(text = "Name") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text(text = "Price") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text(text = "Category") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text(text = "Stock") }
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(16.dp)
        ) {
            Text(text = "Add Product")
        }
    }}
}
