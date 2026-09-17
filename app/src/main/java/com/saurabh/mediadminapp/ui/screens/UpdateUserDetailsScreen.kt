package com.saurabh.mediadminapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayEmptyState
import com.saurabh.mediadminapp.ui.screens.components.ClayPrimaryButton
import com.saurabh.mediadminapp.ui.screens.components.ClayTextField
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayScreenBg
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateUserDetailsScreen(user_id: String, viewModel: MyViewModel, navController: NavController) {
    val context = LocalContext.current
    val usersState = viewModel.getAllUserState.collectAsState().value
    val user = remember(usersState, user_id) {
        usersState.success?.users?.find { it.user_id == user_id }
    }

    LaunchedEffect(Unit) {
        if (usersState.success == null) {
            viewModel.getAllUsers()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ClayScreenBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Update User Profile",
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ClayTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ClayScreenBg
                )
            )
        }
    ) { innerPadding ->
        if (user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(ClayScreenBg),
                contentAlignment = Alignment.Center
            ) {
                ClayEmptyState(
                    message = "User details not found",
                    emoji = "🔍"
                )
            }
        } else {
            var name by remember(user) { mutableStateOf(user.name ?: "") }
            var email by remember(user) { mutableStateOf(user.email ?: "") }
            var phone by remember(user) { mutableStateOf(user.phone_number ?: "") }
            var address by remember(user) { mutableStateOf(user.address ?: "") }
            var pinCode by remember(user) { mutableStateOf(user.pin_code ?: "") }
            var isApproved by remember(user) { mutableStateOf(user.isApproved) }
            var isBlocked by remember(user) { mutableStateOf(user.block) }
            var isSaving by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(ClayScreenBg)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ClayCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Personal Information",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    ClayTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Full Name",
                        leadingIcon = Icons.Default.Person
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ClayTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        leadingIcon = Icons.Default.Email
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ClayTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone Number",
                        leadingIcon = Icons.Default.Phone
                    )
                }

                ClayCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Address & Location",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    ClayTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Street Address",
                        leadingIcon = Icons.Default.Home
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ClayTextField(
                        value = pinCode,
                        onValueChange = { pinCode = it },
                        label = "Pincode",
                        leadingIcon = Icons.Default.LocationOn
                    )
                }

                ClayCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Account Controls",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClayTextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Account Approval", fontWeight = FontWeight.SemiBold, color = ClayTextPrimary)
                            Text("Allow user access to app", fontSize = 12.sp, color = ClayTextSecondary)
                        }
                        Switch(
                            checked = isApproved,
                            onCheckedChange = { isApproved = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ClayPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Block User", fontWeight = FontWeight.SemiBold, color = ClayTextPrimary)
                            Text("Restrict user actions", fontSize = 12.sp, color = ClayTextSecondary)
                        }
                        Switch(
                            checked = isBlocked,
                            onCheckedChange = { isBlocked = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ClayPrimary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ClayPrimaryButton(
                    text = if (isSaving) "Saving Changes..." else "Save Changes",
                    isLoading = isSaving,
                    onClick = {
                        isSaving = true
                        viewModel.isApprovedUser(user.user_id, isApproved)
                        viewModel.updateUser(
                            userId = user.user_id,
                            name = name,
                            email = email,
                            phonenumber = phone,
                            address = address,
                            pincode = pinCode,
                            isApproved = isApproved,
                            block = isBlocked
                        )
                        Toast.makeText(context, "User profile updated successfully!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
