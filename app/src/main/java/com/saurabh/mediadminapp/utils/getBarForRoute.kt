package com.saurabh.mediadminapp.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.ui.screens.nav.HomeRoutes
import com.saurabh.mediadminapp.ui.screens.nav.ProductRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun getTopBarForRoute(route : String?,navController : NavController) : @Composable () -> Unit {
    return when{
        route?.contains("HomeRoutes") == true->{
                        {
                TopAppBar(title = {Text(
                    text = "Admin Management",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 9.dp)
                )},
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Admin Setting")
                        }
                    }
                )
            }
        }

        route?.contains("productroutes") == true->{
                        {
                TopAppBar(title = {Text(
                    text = "All Product",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )},
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Product Setting")
                        }
                    })
            }
        }
        route?.contains("HistoryRoutes") == true->{
            {
                TopAppBar(title = {Text(
                    text = "History",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )},
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "History Setting")
                        }
                    })
            }
        }
        route?.contains("OrdersRoutes") == true->{
                        {
                TopAppBar(title = {Text(
                    text = "Orders Screen",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )},
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Order Setting")
                        }
                    })
            }
        }
        route?.contains("UserSettingsRoutes") == true -> {
            {
                TopAppBar(
                    title = {
                        Text(
                            text = "User Detail Management",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate(HomeRoutes()){
                            popUpTo(HomeRoutes()){inclusive = true}    // on click back button navigate to home screen and cleat back stack entry
                        } }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Home"
                            )
                        }
                    }
                )

            }
        }
        route?.contains("specificProductRoutes") == true -> {
            {
                TopAppBar(
                    title = {
                        Text(
                            text = "ProductDetail",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate(ProductRoutes()){
                            popUpTo(ProductRoutes()){inclusive = true}    // on click back button navigate to product screen and cleat back stack entry
                        } }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Product"
                            )
                        }
                    }
                )

            }
        }
        route?.startsWith("userdetailsroutes") == true->{
            {}

        }

        else -> {{
            TopAppBar(
                title = {
                    Text(
                        text = "App",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            )
        }}
    }
}