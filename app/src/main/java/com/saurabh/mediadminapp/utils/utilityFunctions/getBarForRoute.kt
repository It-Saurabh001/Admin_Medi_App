package com.saurabh.mediadminapp.utils.utilityFunctions

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
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.nav.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun getTopBarForRoute(route: String?, navController: NavController, viewModel: MyViewModel, onMenuClick: () -> Unit) : (@Composable () -> Unit)? {

    return when{
        route?.contains("HomeRoutes") == true ->{
            {
                Column {
                    TopAppBar(title = {Text(
                        text = "Admin Management",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 9.dp)
                    )},
                        actions = {
                            IconButton(onClick = onMenuClick) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Drawer")
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
                        IconButton(onClick = onMenuClick) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Drawer"
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
                            IconButton(onClick = onMenuClick) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Drawer")
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
        route?.contains("ordersRoutes") == true->{
            {
                Column {
                    TopAppBar(title = {Text(
                        text = "Orders Screen",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )},
                        actions = {
                            IconButton(onClick = onMenuClick) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Drawer")
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
                            IconButton(onClick = { navController.navigate(Routes.ProductRoutes()){
                                popUpTo(Routes.ProductRoutes()){inclusive = true}    // on click back button navigate to product screen and cleat back stack entry
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
        route?.contains("updateProductRoutes") == true -> {
            {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Update Product Details",
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
        route?.contains("orderDetailRoutes") == true -> {
            {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Each User Orders",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack()
                            viewModel.clearGetUsersOrdersState()}
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "BackToOrderDetailScreen"
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
        route?.contains("specificOrderRoutes") == true -> {
            {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Specific Order",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack()
                            viewModel.clearGetOrderByIdState()}) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "BackToEachOrderScreen"
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


        // Profile, Settings, About, and unknown routes → no top bar (null)
        else -> null
    }
}