package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.ProductItem

@Composable
fun SpecificProductScreen(productId: String, viewModel: MyViewModel, navController: NavController) {
    val productstate = viewModel.getSpecificProductState.collectAsState()
    LaunchedEffect(key1 = Unit) {
        viewModel.getSpecificProduct(productId)
    }

    Scaffold(){ innerpadding ->

        when {
            productstate.value.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerpadding),
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
                        Text(productstate.value.error.toString(), fontSize = 14.sp, color = Color.Red)
                    }
                }
            }

            productstate.value.success != null -> {
                val productItem = productstate.value.success?.product
                if (productItem != null) {
                    EachProduct(productItem,navController)
                } else {
                    // Handle case when user is not found
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerpadding),
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
fun EachProduct(productItem: ProductItem, navController: NavController) {

//    ElevatedCard (modifier = Modifier
//        .fillMaxWidth()
//        .padding(vertical = 4.dp)
//        .clickable(onClick = {navController.navigate(SpecificProductRoutes.invoke(productItem.Product_id))}),
//        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)){

        Column (modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            HorizontalScrollableText(
                "Id: " + productItem.Product_id, style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)
//                    .padding(top = 13.dp, start = 13.dp, end = 13.dp, bottom = 0.dp)

            )
            HorizontalDivider(modifier = Modifier.padding(start = 10.dp, end = 10.dp))
            HorizontalScrollableText(
                "Name: ${productItem.name}", style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)
//                    .padding(top = 0.dp, start = 13.dp, end = 13.dp, bottom = 0.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(start = 10.dp, end = 10.dp))
            HorizontalScrollableText(
                "Category: " + productItem.category, style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)
//                    .padding(top = 0.dp, start = 13.dp, end = 13.dp, bottom = 0.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(start = 10.dp, end = 10.dp))

            HorizontalScrollableText(
                "Price: " + productItem.price.toString(), style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)
//                    .padding(top = 0.dp, start = 13.dp, end = 13.dp, bottom = 0.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(start = 10.dp, end = 10.dp))
            HorizontalScrollableText(
                "Stock: " + productItem.stock.toString(), style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp)
//                    .padding(top = 0.dp, start = 13.dp, end = 13.dp, bottom = 13.dp)
            )

        }

//    }
}