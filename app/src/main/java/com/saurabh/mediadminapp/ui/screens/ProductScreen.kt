package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.ProductItem
import com.saurabh.mediadminapp.ui.screens.nav.AddProductRoutes
import com.saurabh.mediadminapp.ui.screens.nav.CategoryFilterButton
import com.saurabh.mediadminapp.ui.screens.nav.SpecificProductRoutes
import com.saurabh.mediadminapp.ui.screens.nav.StatsCard
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord
import java.text.NumberFormat
import java.util.Locale


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
//       modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = {navController.navigate(AddProductRoutes.route)}) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ){ innerpadding->


        when{
            productState.value.isLoading->{
                Box(
                    modifier = Modifier
                        .padding(innerpadding)
                        .fillMaxSize()

                ) {
                    LoadingScreen(modifier = Modifier)
                }
            }
            productState.value.error !=null->{
                Box(
                    modifier = Modifier
                        .padding(innerpadding)
                        .fillMaxSize()

                ) {
                    Log.d("TAG", "ProductScreen: error :-> ${productState.value.error}")
                    ErrorScreen(
                        errorMessage = productState.value.error.toString(),
                    )
                }
            }
            productState.value.success != null->{
                Box(
                    modifier = Modifier
                        .fillMaxSize()

                ) {
                    ProductListScreen(
                        productState.value.success!!.products,
                        navController,
                        modifier = Modifier.background(Color(0xFFffffff)),
                        viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun ProductListScreen(products : List<ProductItem>,navController: NavController,modifier: Modifier = Modifier,viewModel: MyViewModel
) {

    var searchTerm by remember { mutableStateOf("") }
    var filterCategory by remember { mutableStateOf("all") }
    val categories = listOf("all", "tablet", "capsule", "liquid", "injection")

    val cardColors = listOf(
        Color(0xFFD3C4FA), // Red
        Color(0xFFACF68F), // Green
        Color(0xFFE9CF87), // Blue
        Color(0xFFFBE89E), // Yellow
        Color(0xFFFACDD4)  // Purple
    )

    // Filter products based on search and category
    val filteredProducts = remember(products, searchTerm, filterCategory) {
        products.filter { product ->
            val matchesSearch = product.name.contains(searchTerm, ignoreCase = true)
            val matchesCategory = filterCategory == "all" ||
                    product.category.lowercase() == filterCategory.lowercase()

            matchesSearch && matchesCategory
        }
    }

    val stats = remember(products) {
        mapOf(
            "total" to products.size,
            "inStock" to products.count { it.stock > 10 },
            "lowStock" to products.count { it.stock in 1..10 },
            "outOfStock" to products.count { it.stock == 0 }
        )
    }

    // Medical gradient colors
    val medicalGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF0EA5E9), // sky-500
            Color(0xFF3B82F6)  // blue-500
        )
    )

    LazyColumn (
        // it shows all products in list
        modifier = modifier.fillMaxSize(),

        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            // stats summary
            Row(
                modifier = Modifier.padding(1.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["total"].toString(),
                    label = "Total Items",
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
//                Spacer(Modifier.width(5.dp))
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["inStock"].toString(),
                    label = "Total Items",
                    valueColor = Color(0xFF10B981)
                )
//                Spacer(Modifier.width(5.dp))
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["lowStock"].toString(),
                    label = "Total Items",
                    valueColor = Color(0xFF0EA5E9)
                )
//                Spacer(Modifier.width(5.dp))
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["outOfStock"].toString(),
                    label = "Total Items",
                    valueColor = Color(0xFFF59EBB)
                )
            }
        }
        item {
            // search bar
            OutlinedTextField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                placeholder = { Text("Search medicines...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        item {
            // filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(categories) { category ->
                    CategoryFilterButton(
                        category = category,
                        isSelected = filterCategory == category,
                        onClick = { filterCategory = category },
                        medicalGradient = medicalGradient
                    )
                }

            }
        }
        if (filteredProducts.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "No products",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No products found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // product list
            itemsIndexed(products) { index, productItem ->
                val bgColor = cardColors[index % cardColors.size]  // serial color
                EachProductCard(productItem, navController, bgColor)

            }
        }
    }
}





@Preview(showBackground = true)
@Composable
fun previe() {
    val navController = rememberNavController()
    val products = ProductItem(
        Product_id="PROD_d1fc4410",
        id = 5,
        name = "Vitamin D3 Tablets",
        price = 35.00,
        category = "Tablet",
        stock = 75
        )
    EachProductCard(products,navController,Color(0xFFD3C4FA))

}


@Composable
fun EachProductCard(medicine: ProductItem,navController: NavController,cardBgColor : Color){
    ElevatedCard ( modifier = Modifier
        .fillMaxWidth()
        .shadow(2.dp, RoundedCornerShape(8.dp)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardBgColor  // serial color
        ),
        shape = RoundedCornerShape(8.dp)){
        Column( modifier = Modifier.fillMaxWidth().padding(5.dp,),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(0.dp),

                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically

                ){
                    HorizontalScrollable1(
                        {
                            Text(
                                "Id: "+medicine.name.capitalizeEachWord(), style = TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .padding(0.dp),
                                maxLines = 1
                            )
                        }
                        )

                    Surface(modifier = Modifier.fillMaxWidth(0.5f),
                        color = when {
                            medicine.stock == 0 -> MaterialTheme.colorScheme.error
                            medicine.stock <= 10 -> Color(0xFFF59E0B) // yellow-500
                            else -> Color(0xFF10B981) // green-500
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = when {
                                medicine.stock == 0 -> "Out of Stock"
                                medicine.stock <= 10 -> "Low Stock"
                                else -> "In Stock"
                            },
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(5.dp),
                            textAlign = TextAlign.Center


                        )
                    }
                }
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = medicine.category.capitalizeEachWord(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                // stock admin mai show hoga
                Text(
                    modifier = Modifier.padding(end =10.dp),
                    text = "Stock: ${medicine.stock}",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Price
            Text(
                modifier = Modifier.padding(3.dp),
                text = "₹${NumberFormat.getInstance(Locale("en", "IN")).format(medicine.price)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary

            )
            // actions button
            Box(modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
                OutlinedButton(
//                    onClick = {},
                    onClick =  { navController.navigate(SpecificProductRoutes.invoke(medicine.Product_id)) },
                    modifier = Modifier.wrapContentSize(),
                    colors = ButtonDefaults.buttonColors(Color(0xFF7089F0))
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "View",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Details")
                }
            }
        }


    }
    
}


