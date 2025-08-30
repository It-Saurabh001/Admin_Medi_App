package com.saurabh.mediadminapp.ui.screens.nav

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.saurabh.mediadminapp.network.response.Order
import com.saurabh.mediadminapp.network.response.ProductItem
import com.saurabh.mediadminapp.network.response.UserItem
import com.saurabh.mediadminapp.ui.screens.EachUserCard1
import com.saurabh.mediadminapp.ui.screens.HorizontalScrollableText
import com.saurabh.mediadminapp.utils.ScreensState.IsApprovedUserState
import com.saurabh.mediadminapp.utils.utilityFunctions.capitalizeEachWord
import java.time.LocalDate

@Preview(showBackground = true)
@Composable
fun test(modifier: Modifier = Modifier) {
    var users by remember { mutableStateOf(mockUsers) }

    
}


@Composable
fun EachUserCard(userItem: UserItem, userApprovalState : State<Map<String , IsApprovedUserState>>, onApprovalToggle: (String, Boolean) -> Unit,navController: NavController){

    val currentUserState = userApprovalState.value[userItem.user_id]
    var isApproved by rememberSaveable(userItem.user_id) {
        mutableStateOf(userItem.isApproved)
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
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {

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
                    Text(text = LocalDate.parse(userItem.date_of_account_creation).toString(), fontSize = 14.sp)
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
fun EachProductCard(productItem: ProductItem, navController: NavController) {
    ElevatedCard (modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp) // clickable mera view se replace ho jayega
        .clickable(onClick = { navController.navigate(SpecificProductRoutes.invoke(productItem.Product_id)) }),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)){
        Row (modifier = Modifier.fillMaxWidth()){
            Column (modifier = Modifier.fillMaxWidth(0.5f)){
                HorizontalScrollableText("Id: \n"+productItem.Product_id.capitalizeEachWord(), style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(13.dp)
                )
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalScrollableText(
                    "Name: \n${ productItem.name.capitalizeEachWord() }", style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(13.dp)
                )
            }
        }
    }
}
@Composable
fun EachOrderCard(order: Order, navController: NavController) {
    ElevatedCard (modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
        .clickable(onClick = { navController.navigate(EachUserOrderRoutes.invoke(order.user_id)) }),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)){
        Row (modifier = Modifier.fillMaxWidth()){
            Column (modifier = Modifier.fillMaxWidth(0.5f)){
                HorizontalScrollableText("Id: \n"+order.order_id, style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(13.dp)
                )
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalScrollableText(
                    "UserId: \n "+order.user_id, style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(13.dp)
                )
                HorizontalScrollableText(
                    "User Name: \n "+order.user_name, style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(13.dp)
                )
            }
        }
    }
}