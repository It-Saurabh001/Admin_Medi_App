package com.saurabh.mediadminapp.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
                Column {
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
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        thickness = 1.dp
                    )
                }

            }
        }

        route?.contains("productroutes") == true->{
            {
                Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "All Product",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Product Setting"
                            )
                        }
                    })
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        thickness = 1.dp
                    )
                }
            }
        }
        route?.contains("HistoryRoutes") == true->{
            {
                Column {
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
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        thickness = 1.dp
                    )
                }

            }
        }
        route?.contains("OrdersRoutes") == true->{
            {
                Column {
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
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        thickness = 1.dp
                    )
                }

            }
        }
        route?.contains("UserSettingsRoutes") == true -> {
            {
                Column {
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
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Home"
                                )
                            }
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        thickness = 1.dp
                    )
                }


            }
        }
        route?.contains("AddProductRoutes") == true -> {
            {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Add Product",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Product"
                                )
                            }
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        thickness = 1.dp
                    )
                }


            }
        }
        route?.contains("specificProductRoutes") == true -> {
            {
                Column {
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
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        thickness = 1.dp
                    )
                }


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