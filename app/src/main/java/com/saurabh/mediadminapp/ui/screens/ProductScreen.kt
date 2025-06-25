package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.ProductItem
import com.saurabh.mediadminapp.ui.screens.nav.AddProductRoutes
import com.saurabh.mediadminapp.ui.screens.nav.SpecificProductRoutes


@Composable
fun ProductScreen(viewModel: MyViewModel, navController: NavController) {
    val productState = viewModel.getAllProduct.collectAsState()
    val currentState = navController.currentBackStackEntry
    LaunchedEffect(currentState) {
        val refresh = currentState?.savedStateHandle?.get<Boolean>("refresh_screen") == true
        if (refresh) {
            viewModel.getAllProduct(force = true)
            currentState.savedStateHandle.remove<Boolean>("refresh_screen")
        }
    }
    LaunchedEffect(key1 = Unit) {
        viewModel.getAllProduct()
    }

    Scaffold (
       modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = {navController.navigate(AddProductRoutes.route)}) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ){ innerpadding->
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
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                thickness = 1.dp
            )
            when{
                productState.value.isLoading->{
                    LoadingScreen(modifier = Modifier.padding(innerpadding))
                }
                productState.value.error !=null->{
                    Log.d("TAG", "ProductScreen: error :-> ${productState.value.error}")
                    ErrorScreen(errorMessage = productState.value.error.toString(), modifier = Modifier.padding(innerpadding))
                }
                productState.value.success != null->{
                    ProductListScreen(productState.value.success!!.products,navController, modifier = Modifier.padding(innerpadding))

                }
            }
        }
    }
}

@Composable
fun ProductListScreen(products : List<ProductItem>,navController: NavController,modifier: Modifier = Modifier) {
    Column(
        // it shows all products in list
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items (products){productItem->
                EachProductCard(productItem ,  navController)
            }
        }

    }


}

@Preview(showBackground = true)
@Composable
fun PreviewProductListScreen() {
    val sampleProducts = listOf(
        ProductItem(
            Product_id = "P001",
            category = "Electronics",
            id = 101,
            name = "Smartphone",
            price = 49999.99,
            stock = 25
        ),
        ProductItem(
            Product_id = "P002",
            category = "Books",
            id = 102,
            name = "Kotlin for Android Developers",
            price = 799.0,
            stock = 100
        ),
        ProductItem(
            Product_id = "P003",
            category = "Appliances",
            id = 103,
            name = "Air Conditioner",
            price = 35999.0,
            stock = 10
        )
    )
    val navController = rememberNavController()
    ProductListScreen(products = sampleProducts, navController = navController)
}

@Composable
fun EachProductCard(productItem: ProductItem, navController: NavController) {
    ElevatedCard (modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
        .clickable(onClick = {navController.navigate(SpecificProductRoutes.invoke(productItem.Product_id))}),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)){
        Row (modifier = Modifier.fillMaxWidth()){
            Column (modifier = Modifier.fillMaxWidth(0.5f)){
                HorizontalScrollableText("Id: \n"+productItem.Product_id, style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                    ),
                    modifier = Modifier.fillMaxWidth().padding(13.dp)
                )
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalScrollableText(
                    "Name: \n${ productItem.name }", style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.fillMaxWidth().padding(13.dp)
                )
            }
        }
    }
}

//@Preview(showBackground = true)
@Composable
fun PreviewEachProductCard() {
    val sampleProduct = ProductItem(
        id = 1,
        Product_id = "ID1234",
        name = "Sample Item",
        category = "Category A",
        price = 199.99,
        stock = 20
    )
    EachProductCard(productItem = sampleProduct, navController = rememberNavController())
}
