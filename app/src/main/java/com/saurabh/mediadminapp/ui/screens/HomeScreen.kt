package com.saurabh.mediadminapp.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.UserItem
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayEmptyState
import com.saurabh.mediadminapp.ui.screens.components.ClayErrorScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayFilterChip
import com.saurabh.mediadminapp.ui.screens.components.FilterOption
import com.saurabh.mediadminapp.ui.screens.components.ClayLoadingScreen
import com.saurabh.mediadminapp.ui.screens.components.ClayOutlinedButton
import com.saurabh.mediadminapp.ui.screens.components.ClaySearchField
import com.saurabh.mediadminapp.ui.screens.components.ClayStatCard
import com.saurabh.mediadminapp.ui.screens.components.ClayStatusBadge
import com.saurabh.mediadminapp.ui.screens.components.DonutChart
import com.saurabh.mediadminapp.ui.screens.components.buildUserStatsSegments
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.ui.theme.ClayBadgeApproved
import com.saurabh.mediadminapp.ui.theme.ClayBadgePending
import com.saurabh.mediadminapp.ui.theme.ClayError
import com.saurabh.mediadminapp.ui.theme.ClayHomeGradient
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayScreenBg
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary
import com.saurabh.mediadminapp.utils.ScreensState.IsApprovedUserState
import java.time.LocalDate


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen1(viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.getAllUserState.collectAsState()
    val isApproved = viewModel.isApprovedUser.collectAsState()

    Log.d("HomeScreen", "HomeScreen composed — state: ${state.isLoading}")

    LaunchedEffect(key1 = Unit) {
        Log.d("HomeScreen", "LaunchedEffect triggered to fetch all users")
        viewModel.getAllUsers()
    }
    
    Scaffold(modifier = Modifier.background(ClayScreenBg)) { innerpadding ->
        when {
            state.isLoading -> {
                ClayLoadingScreen(modifier = Modifier.padding(innerpadding))
            }
            state.error != null -> {
                Log.d("TAG", "HomeScreen:  error :-> ${state.error}")
                ClayErrorScreen(
                    errorMessage = state.error.toString(), 
                    modifier = Modifier.padding(innerpadding)
                )
            }
            state.success != null -> {
                UserListScreen(
                    users = state.success!!.users,
                    userApprovalState = isApproved,
                    onApprovalToggle = viewModel::isApprovedUser,
                    modifier = Modifier
                        .padding(innerpadding)
                        .background(ClayScreenBg),
                    navController = navController
                )
            }
        }
    }
}

enum class FilterStatus { ALL, APPROVED, PENDING, BLOCKED }

@Composable
fun UserListScreen(
    users: List<UserItem>,
    userApprovalState: State<Map<String, IsApprovedUserState>>,
    onApprovalToggle: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    var searchTerm by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf(FilterStatus.ALL) }

    val filteredUsers = remember(users, searchTerm, filterStatus) {
        users.filter { user ->
            val matchesSearch = user.name.lowercase().contains(searchTerm.lowercase()) ||
                    user.email.lowercase().contains(searchTerm.lowercase()) ||
                    user.user_id.lowercase().contains(searchTerm.lowercase())

            val matchesFilter = when (filterStatus) {
                FilterStatus.ALL -> true
                FilterStatus.APPROVED -> user.isApproved && !user.block
                FilterStatus.PENDING -> !user.isApproved
                FilterStatus.BLOCKED -> user.block
            }

            matchesSearch && matchesFilter
        }
    }

    val stats = remember(users) {
        mapOf(
            "total" to users.size,
            "approved" to users.count { it.isApproved && !it.block },
            "pending" to users.count { !it.isApproved },
            "blocked" to users.count { it.block }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ClayCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "User Overview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ClayTextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                DonutChart(
                    segments = buildUserStatsSegments(stats),
                    centerLabel = stats["total"].toString(),
                    centerSubLabel = "Total Users",
                    chartSize = 160.dp
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClayStatCard(
                    modifier = Modifier.weight(1f),
                    value = stats["approved"].toString(),
                    label = "Approved",
                    accentColor = ClayBadgeApproved
                )
                ClayStatCard(
                    modifier = Modifier.weight(1f),
                    value = stats["pending"].toString(),
                    label = "Pending",
                    accentColor = ClayBadgePending
                )
                ClayStatCard(
                    modifier = Modifier.weight(1f),
                    value = stats["blocked"].toString(),
                    label = "Blocked",
                    accentColor = ClayError
                )
            }
        }

        item {
            ClaySearchField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                placeholder = "Search users...",
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
            )
        }

        item {
            val filterOptions = listOf(
                FilterOption(FilterStatus.ALL, "All Users", Icons.AutoMirrored.Filled.List),
                FilterOption(FilterStatus.APPROVED, "Approved", Icons.Default.CheckCircle),
                FilterOption(FilterStatus.PENDING, "Pending", Icons.AutoMirrored.Filled.List),
                FilterOption(FilterStatus.BLOCKED, "Blocked", Icons.Default.Add)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(filterOptions) { filter ->
                    ClayFilterChip(
                        label = filter.label,
                        icon = filter.icon,
                        selected = filterStatus == filter.key,
                        onClick = { filterStatus = filter.key },
                        gradient = ClayHomeGradient
                    )
                }
            }
        }

        if (filteredUsers.isEmpty()) {
            item {
                ClayEmptyState(
                    message = "No users found",
                    emoji = "👥"
                )
            }
        } else {
            items(filteredUsers) { userItem ->
                EachUserCard1(
                    userItem = userItem,
                    userApprovalState = userApprovalState,
                    onApprovalToggle = onApprovalToggle,
                    navController = navController
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun EachUserCard1(
    userItem: UserItem,
    userApprovalState: State<Map<String, IsApprovedUserState>>,
    onApprovalToggle: (String, Boolean) -> Unit,
    navController: NavController
) {
    val currentUserState = userApprovalState.value[userItem.user_id]
    var isApproved by remember(userItem.user_id) {
        mutableStateOf(userItem.isApproved)
    }
    var pendingToggle by rememberSaveable(userItem.user_id) {
        mutableStateOf(false)
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isApproved) 1.1f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "thumbScale"
    )

    LaunchedEffect(currentUserState?.success) {
        if (currentUserState?.success != null && pendingToggle) {
            pendingToggle = false
        }
    }
    LaunchedEffect(currentUserState?.error) {
        if (currentUserState?.error != null && pendingToggle) {
            pendingToggle = false
        }
    }

    val isLoading = currentUserState?.isLoading == true

    ClayCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    HorizontalScrollableText(
                        text = userItem.name,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ClayTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                    HorizontalScrollableText(
                        text = userItem.user_id,
                        style = TextStyle(
                            color = ClayTextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
                
                val (statusText, statusColor) = when {
                    !isApproved -> "Pending" to ClayBadgePending
                    isApproved -> "Approved" to ClayBadgeApproved
                    else -> "Blocked" to ClayError
                }
                
                ClayStatusBadge(text = statusText, color = statusColor)
            }
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = userItem.email ?: "No Email",
                fontSize = 14.sp,
                color = ClayTextSecondary
            )
            Text(
                text = userItem.phone_number ?: "No Phone",
                fontSize = 14.sp,
                color = ClayTextSecondary
            )
            
            val addressText = remember(userItem.address, userItem.pin_code) {
                val addr = userItem.address ?: ""
                val pin = userItem.pin_code ?: ""
                if (addr.isNotEmpty() && pin.isNotEmpty()) "$addr, $pin"
                else addr.ifEmpty { pin }.ifEmpty { "No Address" }
            }
            Text(
                text = addressText,
                fontSize = 14.sp,
                color = ClayTextSecondary
            )
            
            val createdDate = remember(userItem.date_of_account_creation) {
                try {
                    val date = userItem.date_of_account_creation
                    if (date.isNotEmpty()) {
                        LocalDate.parse(date.take(10)).toString()
                    } else {
                        "N/A"
                    }
                } catch (e: Exception) {
                    userItem.date_of_account_creation ?: "N/A"
                }
            }
            Text(
                text = "Created: $createdDate",
                fontSize = 14.sp,
                color = ClayTextSecondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ClayOutlinedButton(
                    text = "Details",
                    onClick = { navController.navigate(Routes.UserDetailsRoutes.invoke(userItem.user_id)) },
                    leadingIcon = Icons.Default.Add,
                    modifier = Modifier.weight(1f)
                )
                
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .fillMaxWidth(0.5f),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(8.dp),
                            strokeWidth = 2.dp,
                            color = ClayPrimary
                        )
                    } else {
                        Switch(
                            checked = isApproved,
                            onCheckedChange = {
                                pendingToggle = true
                                onApprovalToggle(userItem.user_id, it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ClayBadgeApproved,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.LightGray
                            ),
                            enabled = !isLoading,
                            modifier = Modifier.graphicsLayer(
                                scaleX = scale,
                                scaleY = scale
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalScrollableText(
    text: String,
    style: TextStyle = TextStyle.Default,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Text(
        text = text,
        modifier = modifier.horizontalScroll(scrollState),
        style = style,
        maxLines = 1
    )
}
