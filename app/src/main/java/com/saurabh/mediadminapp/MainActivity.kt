package com.saurabh.mediadminapp

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.saurabh.mediadminapp.ui.screens.OrderDetailsScreen1
import com.saurabh.mediadminapp.ui.screens.nav.NavApp
import com.saurabh.mediadminapp.ui.theme.MediAdminAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.importantForAutofill= View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
//        enableEdgeToEdge()
        setContent {
            val viewModel : MyViewModel by viewModels()
            MediAdminAppTheme {
                NavApp(viewModel)
//                TestScreen()

//                OrdersScreen(viewModel)
            }
        }
    }
}



@Composable
fun TestScreen() {
    var text by remember { mutableStateOf("") }
    var clickCount by remember { mutableIntStateOf(0) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Type here") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("You typed: $text")

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { clickCount++ }) {
                Text("Tap me")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Button tapped $clickCount times")
        }
    }
}

