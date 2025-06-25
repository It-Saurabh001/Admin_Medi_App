package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel

@Composable
fun UpdateUserDetailsScreen(user_id : String, viewModel: MyViewModel,navController: NavController) {
    // ViewModel logic can eventually pass state to the content
    UpdateUserDetailsContent()


}
@Composable
fun UpdateUserDetailsContent() {
    Scaffold(modifier = Modifier.fillMaxSize()) {

        Column (modifier = Modifier.padding(it)){

            Text(text = "userdetails screen")
        }
    }
}

//

@Preview(showBackground = true)
@Composable
fun UpdateUserDetailsContentPreview() {
    UpdateUserDetailsContent()
}
