package com.saurabh.mediadminapp.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.network.response.UserItem
import com.saurabh.mediadminapp.ui.screens.nav.UserSettingsRoutes
import com.saurabh.mediadminapp.utils.IsApprovedUserState

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(viewModel: MyViewModel,navController: NavController){
    val state = viewModel.getAllUserState.collectAsState()
    val isApproved = viewModel.isApprovedUser.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.getAllUsers()

    }
    Scaffold (
        modifier = Modifier.fillMaxSize(),

        ){innerpadding->

        when {
            state.value.isLoading ->{
                    LoadingScreen(modifier = Modifier.padding(innerpadding))
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
                    modifier = Modifier.padding(innerpadding).background(Color(0xFFBF360C)),
                    navController= navController

                )
//
            }
        }
    }
}
@Composable
fun EachUserCard(userItem: UserItem, userApprovalState : State<Map<String , IsApprovedUserState>>, onApprovalToggle: (String, Boolean) -> Unit,navController: NavController){

    val currentUserState = userApprovalState.value[userItem.user_id]
    var isApproved by rememberSaveable(userItem.user_id) {
        mutableStateOf(userItem.isApproved == true)
    } // track approval status
    var pendingToggle by rememberSaveable(userItem.user_id) {
        mutableStateOf(false)
    }
    LaunchedEffect(currentUserState?.success) {
        if (currentUserState?.success != null && pendingToggle) {
            isApproved = !isApproved
            pendingToggle = false
        }
    }
    // reset pending flag if we get an error
    LaunchedEffect(currentUserState?.error) {
        if (currentUserState?.error != null && pendingToggle) {
            pendingToggle = false
        }
    }

    val isLoading = currentUserState?.isLoading == true

    ElevatedCard(modifier = Modifier.fillMaxWidth()
        .padding(vertical = 4.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)) {
        Row (modifier = Modifier.fillMaxWidth()){
            Column(modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp))
            {
                // userId and Name
                Row (modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,){

//                    Text(modifier = Modifier.fillMaxWidth(0.5f),text = userItem.name,fontWeight = FontWeight.Bold,
//                        fontSize = 16.sp)
                    HorizontalScrollableText(userItem.name, style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                        ),
                        modifier = Modifier.fillMaxWidth(0.5f).padding(16.dp)
                    )
                    HorizontalScrollableText(userItem.user_id , modifier = Modifier.padding(16.dp),style = TextStyle(
                        color = Color.Gray,
                        fontSize = 12.sp
                    ))
                }
                // account creation date and approval status
                Row (modifier = Modifier.fillMaxWidth()
                    .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(text = userItem.date_of_account_creation, fontSize = 14.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = if (isApproved) "Approved" else "Not Approved",
                            color = if (isApproved) Color(0xFFFFA500) else Color.Gray,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        if(isLoading){
                            CircularProgressIndicator(
                                modifier = Modifier.padding(8.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFFFFA500)
                            )
                        }
                        else{
                            Box(modifier = Modifier.fillMaxWidth()){
                                Switch(             // switch is use as toggle button
                                    checked = isApproved,
                                    onCheckedChange = {
                                        if(!isLoading){
                                            pendingToggle = true   // set pending toggle flag

                                            onApprovalToggle(userItem.user_id,it) // call api to toggle approval
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFFFFA500), // Orange
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = Color.LightGray
                                    ),
                                    enabled = !isLoading
                                )
                            }
                        }
                    }
                }
            }
            Column(modifier = Modifier.fillMaxWidth()
                .padding(top = 5.dp, end = 5.dp),
                horizontalAlignment = AbsoluteAlignment.Right,
                verticalArrangement = Arrangement.Top) {
                IconButton(onClick = {navController.navigate(UserSettingsRoutes.invoke(userItem.user_id))}   // navigation just call the class defined in the routes
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "User Setting")
                }
            }
        }


    }
}

@Composable
fun HorizontalScrollableText(text: String,  style: TextStyle = TextStyle.Default,
                             modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            ,
        style=style

    )
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

@Composable
fun UserListScreen(
    users: List<UserItem>,
    userApprovalState: State<Map<String, IsApprovedUserState>>,
    onApprovalToggle: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(users) { userItem ->
                EachUserCard(
                    userItem = userItem,
                    userApprovalState = userApprovalState,
                    onApprovalToggle = onApprovalToggle,
                    navController = navController
                )
            }
        }
    }
}
