package com.saurabh.mediadminapp.ui.screens


import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.saurabh.mediadminapp.ui.theme.ClayAccent
import com.saurabh.mediadminapp.ui.theme.ClayCardBg
import com.saurabh.mediadminapp.ui.theme.ClayError
import com.saurabh.mediadminapp.ui.theme.ClayFieldBg
import com.saurabh.mediadminapp.ui.theme.ClayGradient
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClaySecondary
import com.saurabh.mediadminapp.ui.theme.ClaySuccess
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary
import kotlinx.coroutines.delay



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsScreen1(user_id: String, viewModel: MyViewModel, navController: NavController) {
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




@Composable
fun UserDetailsScreen(user_id: String, viewModel: MyViewModel, navController: NavController) {
    val usersState = viewModel.getAllUserState.collectAsState()
    val deleteState = viewModel.deleteUserState.collectAsState().value
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        viewModel.getAllUsers()
    }

    LaunchedEffect(key1 = deleteState.success) {
        if (deleteState.success != null && !deleteState.isLoading && deleteState.error == null) {
            Toast.makeText(context, deleteState.success.message, Toast.LENGTH_LONG).show()
            viewModel.resetDeleteUserState()
            navController.navigate(Routes.HomeRoutes()) {
                popUpTo(Routes.HomeRoutes()) { inclusive = true }
            }
        }
    }

    LaunchedEffect(key1 = deleteState.error) {
        if (deleteState.error != null && !deleteState.isLoading) {
            Toast.makeText(context, deleteState.error, Toast.LENGTH_LONG).show()
            viewModel.resetDeleteUserState()
        }
    }

    // Scaffold with Transparent background so our gradient Box shows through
    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        // ── Full-screen vibrant background mirrored from HomeScreen ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = ClayGradient)
        ) {
            // Decorative Blobs matching HomeScreen
            ClayBlob1(
                modifier = Modifier
                    .size(190.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 10.dp),
                color = Color.White.copy(alpha = 0.12f)
            )
            ClayBlob1(
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.BottomStart)
                    .padding(bottom = 50.dp, start = 10.dp),
                color = ClayAccent.copy(alpha = 0.22f)
            )

            // Screen Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    usersState.value.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            BouncingLoader()
                        }
                    }
                    usersState.value.error != null -> {
                        ErrorState(usersState.value.error.toString())
                    }
                    usersState.value.success != null -> {
                        val users = usersState.value.success?.users
                        val user = users?.find { it.user_id == user_id }

                        if (user != null) {
                            SpecificUserDetailedContent(
                                user = user,
                                isDeleting = deleteState.isLoading,
                                onDeleteClick = { viewModel.deleteUser(user.user_id) },
                                onUpdateClick = { navController.navigate(Routes.UpdateUserDetailsRoutes.invoke(user.user_id)) }
                            )
                        } else {
                            ErrorState("User not found or has been deleted")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpecificUserDetailedContent(
    user: UserItem,
    isDeleting: Boolean,
    onDeleteClick: () -> Unit,
    onUpdateClick: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 40.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Hero Profile Section
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { -50 }, animationSpec = tween(600, easing = EaseOutBack))
            ) {
                ProfileHeroSection(user)
            }
        }

        // 2. Contact Details Card
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(700, easing = EaseOutBack))
            ) {
                ClayDetailGroup(title = "Contact Information", icon = Icons.Default.ContactPhone) {
                    DetailItemRow(Icons.Default.Phone, "Phone", user.phone_number ?: "Not Provided")
                    DetailItemRow(Icons.Default.Email, "Email", user.email ?: "Not Provided")
                    DetailItemRow(Icons.Default.LocationOn, "Address", user.address ?: "Not Provided")
                    DetailItemRow(Icons.Default.Map, "Pincode", user.pin_code ?: "Not Provided")
                }
            }
        }

        // 3. Meta Data Card
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { 150 }, animationSpec = tween(800, easing = EaseOutBack))
            ) {
                ClayDetailGroup(title = "Account Metadata", icon = Icons.Default.ManageAccounts) {
                    DetailItemRow(Icons.Default.Badge, "User ID", user.user_id)
                    DetailItemRow(Icons.Default.CalendarToday, "Created On", user.date_of_account_creation ?: "Unknown")
                    DetailItemRow(Icons.Default.Storage, "Database ID", user.id.toString())
                }
            }
        }

        // 4. Action Buttons
        item {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { 200 }, animationSpec = tween(900, easing = EaseOutBack))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AnimatedClayButton(
                        text = "Edit Profile Details",
                        icon = Icons.Default.Edit,
                        gradient = Brush.horizontalGradient(listOf(ClayPrimary, ClaySecondary)),
                        onClick = onUpdateClick,
                        enabled = !isDeleting
                    )

                    AnimatedClayButton(
                        text = if (isDeleting) "Processing Deletion..." else "Suspend / Delete User",
                        icon = Icons.Default.DeleteForever,
                        gradient = Brush.horizontalGradient(listOf(Color(0xFFFF6584), ClayError)),
                        onClick = onDeleteClick,
                        enabled = !isDeleting,
                        isLoading = isDeleting
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileHeroSection(user: UserItem) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "float_offset"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating Avatar
        Box(
            modifier = Modifier
                .offset(y = floatOffset.dp)
                .size(120.dp)
                .shadow(24.dp, CircleShape, ambientColor = ClayPrimary, spotColor = ClayPrimary)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(ClayCardBg, Color.White)))
                .border(4.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.name.take(1).uppercase(),
                fontSize = 54.sp,
                fontWeight = FontWeight.ExtraBold,
                color = ClayPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = user.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White // Updated to White for gradient background contrast
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Status Badges Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusPill(
                isActive = user.isApproved,
                activeText = "Verified Member",
                inactiveText = "Pending Verification",
                activeColor = ClaySuccess,
                inactiveColor = ClayAccent
            )

            if (user.block) {
                StatusPill(
                    isActive = true,
                    activeText = "Account Blocked",
                    inactiveText = "",
                    activeColor = ClayError,
                    inactiveColor = ClayError
                )
            }
        }
    }
}

@Composable
fun StatusPill(isActive: Boolean, activeText: String, inactiveText: String, activeColor: Color, inactiveColor: Color) {
    val color = if (isActive) activeColor else inactiveColor
    val text = if (isActive) activeText else inactiveText

    // Adjusted opacity slightly for better visibility on dark gradient
    Row(
        modifier = Modifier
            .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = color, spotColor = color)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White) // Solid white background for pills to pop against gradient
            .border(1.5.dp, color.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(text = text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ClayDetailGroup(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(32.dp), ambientColor = Color.Black.copy(alpha = 0.2f))
            .clip(RoundedCornerShape(32.dp))
            .background(ClayCardBg)
            .border(2.dp, Color.White, RoundedCornerShape(32.dp))
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ClayFieldBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = ClayPrimary, modifier = Modifier.size(20.dp))
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
fun DetailItemRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ClayFieldBg) // Recessed clay effect
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = ClayTextSecondary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 12.sp, color = ClayTextSecondary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                color = ClayTextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AnimatedClayButton(
    text: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val scale by animateFloatAsState(
        targetValue = if (isLoading) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "button_scale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = if (enabled) 12.dp else 0.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = ClayPrimary,
                spotColor = ClayPrimary
            )
            .clip(RoundedCornerShape(24.dp))
            .background(if (enabled) gradient else Brush.linearGradient(listOf(Color.LightGray, Color.Gray)))
            .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .clickable(enabled = enabled && !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}


@Composable
fun BouncingLoader() {
    val infiniteTransition = rememberInfiniteTransition(label = "loader")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "loader_scale"
    )

    Box(
        modifier = Modifier
            .size(60.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(16.dp, CircleShape, ambientColor = ClayPrimary)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color.White, ClayFieldBg)))
            .border(2.dp, ClayPrimary, CircleShape)
    )
}

@Composable
fun ErrorState(message: String) {
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