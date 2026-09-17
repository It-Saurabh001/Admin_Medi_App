package com.saurabh.mediadminapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayDangerButton
import com.saurabh.mediadminapp.ui.screens.components.ClayInfoRow
import com.saurabh.mediadminapp.ui.screens.components.ClayPrimaryButton
import com.saurabh.mediadminapp.ui.screens.nav.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsScreen(user_id: String, viewModel: MyViewModel, navController: NavController) {
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
            navController.navigate(Routes.HomeRoutes()) {
                popUpTo(Routes.HomeRoutes()) { inclusive = true }
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

    Scaffold() { innerpadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerpadding)
        ) {
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
                            navController = navController,
                            viewModel = viewModel,
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
        }
    }
}

@Composable
fun SpecificUser(
    user: UserItem,
    isDeleting: Boolean,
    onDeleteClick: () -> Unit,
    navController: NavController,
    viewModel: MyViewModel,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "User Details",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        ClayCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            ClayInfoRow("Name", user.name)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFD0C8FF))
            ClayInfoRow("ID", user.id.toString())
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFD0C8FF))
            ClayInfoRow("User ID", user.user_id)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFD0C8FF))
            ClayInfoRow("Account Created", user.date_of_account_creation)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFD0C8FF))
            ClayInfoRow("Approved Status", user.isApproved.toString())
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFD0C8FF))
            ClayInfoRow("Block Status", user.block.toString())
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFD0C8FF))
            ClayInfoRow("Phone Number", user.phone_number)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFD0C8FF))
            ClayInfoRow("Email", user.email)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFD0C8FF))
            ClayInfoRow("Pin Code", user.pin_code)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFD0C8FF))
            ClayInfoRow("Address", user.address)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ClayPrimaryButton(
                text = "Update Details",
                onClick = { navController.navigate(Routes.UpdateUserDetailsRoutes.invoke(user.user_id)) },
                enabled = !isDeleting
            )
            Spacer(modifier = Modifier.height(16.dp))
            ClayDangerButton(
                text = if (isDeleting) "Deleting..." else "Delete User",
                onClick = onDeleteClick,
                isLoading = isDeleting,
                enabled = !isDeleting
            )
        }
    }
}
