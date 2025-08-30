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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.UserItem
import com.saurabh.mediadminapp.ui.screens.nav.EachUserOrderRoutes
import com.saurabh.mediadminapp.ui.screens.nav.FilterButton
import com.saurabh.mediadminapp.ui.screens.nav.FilterOption1
import com.saurabh.mediadminapp.ui.screens.nav.FilterStatus
import com.saurabh.mediadminapp.ui.screens.nav.StatsCard
import com.saurabh.mediadminapp.ui.screens.nav.UserSettingsRoutes
import com.saurabh.mediadminapp.ui.screens.nav.mockUsers
import com.saurabh.mediadminapp.utils.ScreensState.IsApprovedUserState
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(viewModel: MyViewModel,navController: NavController){
    val state = viewModel.getAllUserState.collectAsState()
    val isApproved = viewModel.isApprovedUser.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.getAllUsers()
    }
    Scaffold{innerpadding->
        when {
            state.value.isLoading ->{
                    LoadingScreen(modifier = Modifier)
            }
            state.value.error != null->{
                Log.d("TAG", "HomeScreen:  error :-> ${state.value.error}")
                ErrorScreen(errorMessage = state.value.error.toString(),modifier = Modifier.padding(innerpadding))
            }
            state.value.success != null ->{

                UserListScreen(
                    users = state.value.success!!.users,
                    userApprovalState = isApproved,
                    onApprovalToggle = viewModel::isApprovedUser,
                    modifier = Modifier.background(Color(0xFFffffff)),
                    navController= navController
                )//
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
    var userList by remember { mutableStateOf(users) }
    var searchTerm by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf(FilterStatus.ALL) }

    // Filter users based on search term and filter status
    val filteredUsers = users.filter { user ->
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
    // Calculate stats
    val stats = mapOf(
                "total" to users.size,
                "approved" to users.count { it.isApproved && !it.block },
                "pending" to users.count { !it.isApproved },
                "blocked" to users.count { it.block }
            )



    // Medical gradient colors
    val medicalGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF4F46E5), // Indigo
            Color(0xFF06B6D4)  // Cyan
        )
    )
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Row(
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["total"].toString(),
                    label = "Total Users",
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["approved"].toString(),
                    label = "Approved",
                    valueColor = Color(0xFF4F46E5)
                )
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["pending"].toString(),
                    label = "Pending",
                    valueColor = Color(0xFFD97706)
                )
                StatsCard(
                    modifier = Modifier.weight(1f),
                    value = stats["blocked"].toString(),
                    label = "Blocked",
                    valueColor = MaterialTheme.colorScheme.error
                )
            }
        }
        // summary of stats


        item {
            // Search and Filter
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Search Input
                OutlinedTextField(
                    value = searchTerm,
                    onValueChange = { searchTerm = it },
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    placeholder = { Text("Search users...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(28.dp).padding(start = 3.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },

                    maxLines = 2,
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                // Filter Buttons
                val filterOptions = listOf(
                    FilterOption1(FilterStatus.ALL, "All Users", Icons.AutoMirrored.Filled.List),
                    FilterOption1(FilterStatus.APPROVED, "Approved", Icons.Default.CheckCircle),
                    FilterOption1(FilterStatus.PENDING, "Pending", Icons.AutoMirrored.Filled.List),
                    FilterOption1(FilterStatus.BLOCKED, "Blocked", Icons.Default.Add)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(filterOptions) { filter ->
                        FilterButton(
                            filterOption = filter,
                            isSelected = filterStatus == filter.key,
                            onClick = { filterStatus = filter.key },
                            medicalGradient = medicalGradient
                        )
                    }
                }
            }
        }

        if (filteredUsers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No users found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

        } else {
//            LazyColumn(
//                modifier = Modifier.fillMaxWidth(),
//                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp)
//            ) {
                items(filteredUsers) { userItem ->
                    EachUserCard1(
                        userItem = userItem,
                        userApprovalState = userApprovalState,
                        onApprovalToggle =onApprovalToggle,
                        navController = navController
                    )
                }
//            }
        }
    }
}


@Composable
fun EachUserCard1(
    userItem: UserItem,
    userApprovalState : State<Map<String , IsApprovedUserState>>,
    onApprovalToggle: (String, Boolean) -> Unit,
    navController: NavController
){
    val currentUserState = userApprovalState.value[userItem.user_id]
    var isApproved by rememberSaveable(userItem.user_id) {
        mutableStateOf(userItem.isApproved)
    } // track approval status
    var pendingToggle by rememberSaveable(userItem.user_id) {
        mutableStateOf(false)
    }
    val scale by animateFloatAsState(
        targetValue = if (isApproved) 1.2f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "thumbScale"
    )

    LaunchedEffect(currentUserState?.success) {
        if (currentUserState?.success != null && pendingToggle) {
            isApproved = !isApproved
            pendingToggle = false


        }
    }
//     reset pending flag if we get an error
    LaunchedEffect(currentUserState?.error) {
        if (currentUserState?.error != null && pendingToggle) {
            pendingToggle = false
        }
    }
//
    val isLoading = currentUserState?.isLoading == true

    ElevatedCard(modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {

        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row (modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // userId and Name
                    HorizontalScrollableText(
                        userItem.name, style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.5f)

                    )
                    HorizontalScrollableText(
                        userItem.user_id, modifier = Modifier, style = TextStyle(
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    )

                }
                val (statusText, statusColor) = when {
                    !isApproved -> "Pending" to MaterialTheme.colorScheme.error
                    isApproved -> "Approved" to Color(0xFF10B981)
                    else -> "Blocked" to Color(0xFFD97706)
                }

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // User details
            Text(
                text = userItem.email,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = userItem.phone_number,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${userItem.address}, ${userItem.pin_code}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Created: "+LocalDate.parse(userItem.date_of_account_creation).toString(),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row (modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){

                OutlinedButton(
                    onClick = { navController.navigate(UserSettingsRoutes.invoke(userItem.user_id))},
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF4F46E5)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "View",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Details", fontSize = 14.sp)
                }
                Box(modifier = Modifier.wrapContentSize().fillMaxWidth(0.5f),
                    contentAlignment = Alignment.Center
                ){
                    if(isLoading){
                        CircularProgressIndicator(
                            modifier = Modifier.padding(8.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFFFFA500)
                        )
                    }
                    else {
                        Switch(
                            checked = isApproved,
                            onCheckedChange = {
                                if (!isLoading) {
                                    pendingToggle = true
                                    onApprovalToggle(userItem.user_id, it)


                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFFFA500),
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
    modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Text(
        text = text,
        modifier = modifier.horizontalScroll(scrollState),
        style=style,



    )
}
@Composable
fun HorizontalScrollable1(content: @Composable () -> Unit) {
    val scrollState = rememberScrollState()
    Box(modifier = Modifier.horizontalScroll(scrollState)) {
        content()
    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier){
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "This is loading screen")

        Log.d("TAG", "WaitingScreen: success")

        CircularProgressIndicator()
    }

}

@Composable
fun ErrorScreen(errorMessage: String, modifier: Modifier = Modifier) {
   Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )
        Text(
            text = errorMessage,
            modifier = Modifier.padding(16.dp)
        )
    }
}
