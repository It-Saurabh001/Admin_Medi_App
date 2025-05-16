package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.UserItem
import com.saurabh.mediadminapp.ui.screens.nav.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingScreen(user_id : String, viewModel: MyViewModel,navController: NavController) {
    val usersState = viewModel.getAllUserState.collectAsState()
    val deleteState = viewModel.deleteUserState.collectAsState().value
    val context = LocalContext.current

    // initial data fetch
    LaunchedEffect(key1 = Unit) {
        viewModel.getAllUsers()
    }

    // Handle deletion Success
    LaunchedEffect(key1 = deleteState.success) {
        if (deleteState.success != null && !deleteState.isLoading && deleteState.error == null) {
            Toast.makeText(context, deleteState.success.message, Toast.LENGTH_LONG).show()
            viewModel.resetDeleteUserState()

            // Navigate first, then fetch updated data on the home screen
            navController.navigate(Routes.HomeRoutes) {
                popUpTo(Routes.HomeRoutes) { inclusive = true }
            }
        }
    }

    // Handle deletion error separately
    LaunchedEffect(key1 = deleteState.error) {
        if (deleteState.error != null && !deleteState.isLoading) {
            Log.d("TAG", "UserSettingScreen: error : ${deleteState.error}")
            Toast.makeText(context, deleteState.error, Toast.LENGTH_LONG).show()
            viewModel.resetDeleteUserState()
        }
    }


    Scaffold(
        topBar = {
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
                    IconButton(onClick = { navController.navigate(Routes.HomeRoutes) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Home"
                        )
                    }
                }
            )


        },
    ) { innerpadding ->

        when {
            usersState.value.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerpadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            usersState.value.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerpadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error loading user data", fontSize = 18.sp)
                        Text(usersState.value.error.toString(), fontSize = 14.sp, color = Color.Red)
                    }
                }
            }

            usersState.value.success != null -> {
                val users = usersState.value.success?.users
                val user = users?.find { it.user_id == user_id }

                if (user != null) {
                    SpecificUser(
                        user = user,
                        isDeleting = deleteState.isLoading,
                        onDeleteClick = { viewModel.deleteUser(user.user_id) },
                        modifier = Modifier.padding(innerpadding)
                    )
                } else {
                    // Handle case when user is not found
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerpadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("User not found or has been deleted", fontSize = 18.sp)
                    }
                }
            }
        }
//                    val users = state.value.success!!.users
//                    val user = users.find { it.user_id == user_id }
//                    if(user!=null) {
//                        SpecificUser(user, viewModel = viewModel,navController = navController,modifier = Modifier.padding(innerpadding))
//                    }


    }

}

@Composable
fun SpecificUser(
    user: UserItem,
    isDeleting: Boolean,
    onDeleteClick: () -> Unit,
    modifier: Modifier
) {
//fun SpecificUser(user : UserItem, viewModel: MyViewModel,navController: NavController,modifier: Modifier) {
//    val deleteState = viewModel.deleteUserState.collectAsState().value
//    val context = LocalContext.current
//
//    LaunchedEffect(key1 = deleteState.success) {
//    // Handle deletion Success
//        if(deleteState.success != null && !deleteState.isLoading && deleteState.error == null){
//            Toast.makeText(context, deleteState.success.message, Toast.LENGTH_LONG).show()
//
//
//
//            viewModel.resetDeleteUserState()
//           // fetch updated list
////            viewModel.getAllUsers()
//            // navigate back using popup instead direct navigation
//            navController.navigate(Routes.HomeRoutes){
//                popUpTo(Routes.HomeRoutes){inclusive = true}
//            }
//        }
//    }
//    // Handle deletion error
//    if(deleteState.error != null && !deleteState.isLoading){
//        Log.d("TAG", "SpecificUser: error : ${deleteState.error}")
//        Toast.makeText(context, deleteState.error, Toast.LENGTH_LONG).show()
//        viewModel.resetDeleteUserState()
//    }


    val userDetails = listOf(
        "ID" to user.id.toString(),
        "User ID" to user.user_id.toString(),
        "Date of Account Creation" to user.date_of_account_creation.toString(),
        "isApproved" to user.isApproved.toString(),
        "Block" to user.block.toString(),
        "Name" to user.name.toString(),
        "Phone Number" to user.phone_number.toString(),
        "Email" to user.email.toString(),
        "Pin Code" to user.pin_code.toString(),
        "Address" to user.address.toString(),

        )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            items(userDetails) { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "$label:",
                            modifier = Modifier.align(Alignment.Start),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = value.toString(),
                            modifier = Modifier.align(Alignment.End),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                HorizontalDivider()
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick =
//                        {
//                        viewModel.deleteUser(user.user_id)
//                    },
                            onDeleteClick,
                        enabled = !isDeleting
                    ) {
                        Text(text = if (isDeleting) "Deleting..." else "Delete Button")
                    }
                }
            }
        }
    }
}

