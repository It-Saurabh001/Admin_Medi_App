package com.saurabh.mediadminapp.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.TopAppBar
import com.saurabh.mediadminapp.ui.theme.ClayHomeGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateUserDetailsScreen1(user_id: String, viewModel: MyViewModel, navController: NavController) {
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

private val ClayPrimary = Color(0xFF6C63FF)
private val ClaySecondary = Color(0xFF48CAE4)
private val ClayAccent = Color(0xFFFF6584)
private val ClayCardBg = Color(0xFFFAF9FF)
private val ClayFieldBg = Color(0xFFF0EEFF)
private val ClayTextPrimary = Color(0xFF1E293B)
private val ClayTextSecondary = Color(0xFF64748B)
private val ClaySuccess = Color(0xFF10B981)

val ClayBgGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF6C63FF), Color(0xFF48CAE4))
)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateUserDetailsScreen(user_id: String, viewModel: MyViewModel, navController: NavController) {
    val usersState = viewModel.getAllUserState.collectAsState().value
    val user = remember(usersState, user_id) {
        usersState.success?.users?.find { it.user_id == user_id }
    }

    LaunchedEffect(Unit) {
        if (usersState.success == null) {
            viewModel.getAllUsers()
        }
    }

    // Wrap in a Box to hold the background gradient and decorative blobs globally
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = ClayHomeGradient)
    ) {
        // Decorative Background Blobs
        ClayBlob(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 20.dp),
            color = Color.White.copy(alpha = 0.12f)
        )
        ClayBlob(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.BottomStart)
                .padding(bottom = 60.dp, start = 10.dp),
            color = ClayAccent.copy(alpha = 0.2f)
        )

        // Scaffold with transparent background so gradient shows through the TopBar
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Update UserDetail",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 12.dp)
                                .size(44.dp)
                                .shadow(8.dp, CircleShape, ambientColor = ClayPrimary)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                .clickable { navController.popBackStack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    usersState.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            BouncingLoader()
                        }
                    }
                    user == null && !usersState.isLoading -> {
                        EmptyOrErrorState(message = "User details not found or deleted")
                    }
                    user != null -> {
                        UpdateProfileForm(user = user, viewModel = viewModel, navController = navController)
                    }
                }
            }
        }
    }
}
@Composable
fun UpdateProfileForm(
    user: com.saurabh.mediadminapp.network.response.UserItem,
    viewModel: MyViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    // State Variables
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {



        // Section 1: Personal Details
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(600))
        ) {
            ClayFormCard(title = "Personal Information", icon = Icons.Default.Person) {
                ClayInputField(value = name, onValueChange = { name = it }, label = "Full Name", icon = Icons.Default.Person)
                ClayInputField(value = email, onValueChange = { email = it }, label = "Email Address", icon = Icons.Default.Email)
                ClayInputField(value = phone, onValueChange = { phone = it }, label = "Phone Number", icon = Icons.Default.Phone)
            }
        }

        // Section 2: Location
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { 150 }, animationSpec = tween(700))
        ) {
            ClayFormCard(title = "Address & Location", icon = Icons.Default.LocationOn) {
                ClayInputField(value = address, onValueChange = { address = it }, label = "Street Address", icon = Icons.Default.Home)
                ClayInputField(value = pinCode, onValueChange = { pinCode = it }, label = "Pincode", icon = Icons.Default.LocationOn)
            }
        }

        // Section 3: Admin Controls
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { 200 }, animationSpec = tween(800))
        ) {
            ClayFormCard(title = "Account Controls", icon = Icons.Default.AdminPanelSettings) {
                ClaySwitchRow(
                    title = "Verified Status",
                    subtitle = "Grant user platform access",
                    isChecked = isApproved,
                    onCheckedChange = { isApproved = it },
                    activeColor = ClaySuccess
                )
                ClaySwitchRow(
                    title = "Block User",
                    subtitle = "Restrict all user actions",
                    isChecked = isBlocked,
                    onCheckedChange = { isBlocked = it },
                    activeColor = ClayAccent
                )
            }
        }

        // Save Button
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { 250 }, animationSpec = tween(900))
        ) {
            AnimatedClayButton(
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
                }
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ── CHEERFUL CLAYMORPHISM COMPONENTS ───────────────────────────────────────

@Composable
fun ClayTopHeader(title: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(8.dp, CircleShape, ambientColor = ClayPrimary)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
                .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}

@Composable
fun ClayFormCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(32.dp), ambientColor = Color.Black.copy(alpha = 0.2f))
            .clip(RoundedCornerShape(32.dp))
            .background(ClayCardBg)
            .border(2.dp, Color.White, RoundedCornerShape(32.dp))
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ClayFieldBg)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = ClayPrimary, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ClayTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            content()
        }
    }
}

@Composable
fun ClayInputField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector) {
    Column {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = ClayTextSecondary,
            modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = {
                Icon(icon, contentDescription = null, tint = ClayPrimary)
            },
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ClayPrimary,
                unfocusedBorderColor = Color.Transparent, // No border until focused, makes it look recessed
                focusedContainerColor = Color.White,
                unfocusedContainerColor = ClayFieldBg, // Darker bg for recessed clay effect
                cursorColor = ClayPrimary,
                focusedTextColor = ClayTextPrimary,
                unfocusedTextColor = ClayTextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (value.isNotEmpty()) 4.dp else 0.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = ClayPrimary
                )
        )
    }
}

@Composable
fun ClaySwitchRow(title: String, subtitle: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit, activeColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ClayFieldBg)
            .border(1.dp, Color.White, RoundedCornerShape(20.dp))
            .clickable { onCheckedChange(!isChecked) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ClayTextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 12.sp, color = ClayTextSecondary)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = activeColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBD5E1),
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent
            ),
            modifier = Modifier.shadow(4.dp, CircleShape, ambientColor = activeColor)
        )
    }
}

@Composable
fun AnimatedClayButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    val scale by animateFloatAsState(
        targetValue = if (isLoading) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "button_scale"
    )
    val gradient = Brush.horizontalGradient(listOf(Color(0xFF6C63FF), Color(0xFF48CAE4)))

    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .fillMaxWidth()
            .height(64.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = ClayPrimary)
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = text, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}





@Composable
fun EmptyOrErrorState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = Color.White, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Oops! Something went wrong", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)
    }
}