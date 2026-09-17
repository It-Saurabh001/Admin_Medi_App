package com.saurabh.mediadminapp.ui.screens



import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController,viewModel: MyViewModel) {

    val isUserLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    Log.d("TAG", "SplashScreen: ")
    LaunchedEffect(Unit) {
        delay(2000) // 2 seconds का delay

    }
    Scaffold(
        containerColor = Color.White
    ) {
            innerPadding ->
        Column (modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Image(imageVector = Icons.Default.Person,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f),
                contentScale = ContentScale.Fit,
                contentDescription = "Splash Image")

        }
    }

}