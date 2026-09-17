package com.saurabh.mediadminapp.ui.screens


import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.nav.Routes

@Composable
fun SignIn(viewModel: MyViewModel, navController: NavHostController) {
    Log.d("PERF_TRACE", "SignIn (SignUp composable) START [Thread: ${Thread.currentThread().name}]")
    val state = viewModel.loginAdminState.collectAsState()
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val context  = LocalContext.current
    Log.d("TAG", "SignIn: state: ${state.value}")
    Log.d("PERF_TRACE", "SignIn (SignUp composable) END [Thread: ${Thread.currentThread().name}]")

    LaunchedEffect(state.value.error) {
        if (state.value.error != null) {
            Log.d("TAG", "SignIn error : ${state.value.error.toString()} ")
            Toast.makeText(context, state.value.error.toString(), Toast.LENGTH_LONG).show()
            viewModel.clearLoginState()
        }
    }

    LaunchedEffect(state.value.success) {
        state.value.success?.let { successResponse ->
            if (successResponse.status == 200 && successResponse.admin_id != null) {
                Log.d("TAG", "SignIn success: OTP sent. Navigating to OTP verification for: ${successResponse.admin_id}")
                navController.navigate(Routes.VerifyOtpRoutes(userId = successResponse.admin_id))
                viewModel.clearLoginState()
            } else {
                val errMsg = successResponse.message ?: "Invalid email or password"
                Log.d("TAG", "SignIn backend error: $errMsg (status: ${successResponse.status})")
                Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                viewModel.clearLoginState()
            }
        }
    }

    if (state.value.isLoading) {
        Scaffold { innerpadding->
            Column (modifier = Modifier
                .fillMaxSize()
                .padding(innerpadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally){
                CircularProgressIndicator()
            }
        }
        return
    }



    Scaffold (
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF5F5F5),
    ){ innerpadding->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(innerpadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            // Email
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = "Email")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Password Field
            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            // Login Button

            Button(onClick = {
                viewModel.loginAdmin(email.value, password.value)}
            ) {
                Text(text = "Sign In")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Forgot Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {}) {
                    Text(
                        text = "Forgot Password?",
                        color = Color(0xFF2196F3),
                        fontSize = 14.sp
                    )
                }
            }
            Text(text = "If does not have an account click SignUp button")
            Button(onClick = { navController.navigate(Routes.SignUpRoutes) }) {
                Text(text = "SignUp")
            }
        }
    }

}

