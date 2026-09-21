package com.saurabh.mediadminapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.UserItem
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayEmptyState
import com.saurabh.mediadminapp.ui.screens.components.ClayGradientBackdrop
import com.saurabh.mediadminapp.ui.screens.components.ClayPrimaryButton
import com.saurabh.mediadminapp.ui.screens.components.ClayTextField
import com.saurabh.mediadminapp.ui.theme.ClayBadgeApproved
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayStateBlocked
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary

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

    Scaffold(containerColor = Color.Transparent) { innerPadding ->
        ClayGradientBackdrop {
            if (user == null) {
                ClayEmptyState(
                    message = "User details not found",
                    emoji = "🔍",
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
                UpdateUserForm(
                    user = user,
                    viewModel = viewModel,
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun UpdateUserForm(
    user: UserItem,
    viewModel: MyViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var name by remember(user) { mutableStateOf(user.name) }
    var email by remember(user) { mutableStateOf(user.email) }
    var phone by remember(user) { mutableStateOf(user.phone_number) }
    var address by remember(user) { mutableStateOf(user.address) }
    var pinCode by remember(user) { mutableStateOf(user.pin_code) }
    var isApproved by remember(user) { mutableStateOf(user.isApproved) }
    var isBlocked by remember(user) { mutableStateOf(user.block) }
    var isSaving by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp)
    ) {
        // ── 1. TOP APP BAR ───────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Update User Profile",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // ── 2. PERSONAL INFORMATION FORM CLUSTER ─────────────────────────
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = ClayPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Personal Identity",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClayTextPrimary
                        )
                    }

                    ClayTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Full Name",
                        leadingIcon = Icons.Default.Person
                    )

                    ClayTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        leadingIcon = Icons.Default.Email
                    )

                    ClayTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Phone Number",
                        leadingIcon = Icons.Default.Phone
                    )
                }
            }
        }

        // ── 3. LOCATION & SHIPPING CLUSTER ───────────────────────────────
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = ClayPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Address & Shipping",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClayTextPrimary
                        )
                    }

                    ClayTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Street Address",
                        leadingIcon = Icons.Default.Home
                    )

                    ClayTextField(
                        value = pinCode,
                        onValueChange = { pinCode = it },
                        label = "Postal Pincode",
                        leadingIcon = Icons.Default.LocationOn
                    )
                }
            }
        }

        // ── 4. SECURITY & CLEARANCE SWITCH DECK ───────────────────────────
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = ClayPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Clearance & Access Decks",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClayTextPrimary
                        )
                    }

                    // Account Approval Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Account Clearance",
                                fontWeight = FontWeight.SemiBold,
                                color = ClayTextPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Allow user full access to procurement app",
                                fontSize = 12.sp,
                                color = ClayTextSecondary
                            )
                        }
                        Switch(
                            checked = isApproved,
                            onCheckedChange = { isApproved = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ClayBadgeApproved,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFD0C8FF)
                            )
                        )
                    }

                    // Block Account Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Account Suspension",
                                fontWeight = FontWeight.SemiBold,
                                color = ClayTextPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Restrict user from placing or viewing orders",
                                fontSize = 12.sp,
                                color = ClayTextSecondary
                            )
                        }
                        Switch(
                            checked = isBlocked,
                            onCheckedChange = { isBlocked = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ClayStateBlocked,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFD0C8FF)
                            )
                        )
                    }
                }
            }
        }

        // ── 5. PRIMARY SAVE ACTION CTA ────────────────────────────────────
        item {
            ClayPrimaryButton(
                text = if (isSaving) "Saving Changes..." else "Save Changes",
                icon = Icons.Default.Save,
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
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun UpdateUserDetailsScreen1(user_id: String, viewModel: MyViewModel, navController: NavController) {
    UpdateUserDetailsScreen(user_id = user_id, viewModel = viewModel, navController = navController)
}