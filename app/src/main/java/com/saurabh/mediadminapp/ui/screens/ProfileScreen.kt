package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.screens.components.ClayCard
import com.saurabh.mediadminapp.ui.screens.components.ClayDangerButton
import com.saurabh.mediadminapp.ui.screens.components.ClayInfoRow
import com.saurabh.mediadminapp.ui.theme.ClayPrimary
import com.saurabh.mediadminapp.ui.theme.ClayScreenBg
import com.saurabh.mediadminapp.ui.theme.ClayTextPrimary
import com.saurabh.mediadminapp.ui.theme.ClayTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MyViewModel, navController: NavController) {
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.getAllAdmin()
    }

    val adminState by viewModel.getAllAdminState.collectAsState()
    val loggedInAdminId by viewModel.loggedInAdminId.collectAsState()
    val admin = adminState.success?.admins?.find { it.admin_id == loggedInAdminId }

    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        containerColor = ClayScreenBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = ClayTextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ClayScreenBg),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ClayTextPrimary
                        )
                    }
                }
            )
        }
    ) { innerpadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerpadding)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalDivider(
                modifier = Modifier.padding(bottom = 16.dp),
                thickness = 1.dp,
                color = Color(0xFFD0C8FF)
            )

            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val initial = admin?.name?.firstOrNull()?.toString()?.uppercase() ?: "A"
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .border(3.dp, Color(0xFFD0C8FF), CircleShape)
                        .clip(CircleShape)
                        .background(ClayPrimary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initial,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = admin?.name ?: "Loading...",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ClayTextPrimary
                )
                Text(
                    text = admin?.role?.uppercase() ?: "ADMIN",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ClayTextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))

            ClayCard(
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                ClayInfoRow(label = "Email", value = admin?.email ?: "...")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFD0C8FF))
                ClayInfoRow(label = "Phone", value = admin?.phone_number ?: "...")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFD0C8FF))
                ClayInfoRow(label = "Account Created", value = admin?.date_of_account_creation ?: "...")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFD0C8FF))
                ClayInfoRow(label = "Admin ID", value = admin?.admin_id ?: "...")
            }
            
            Spacer(modifier = Modifier.height(40.dp))

            ClayDangerButton(
                text = "Logout",
                onClick = {
                    viewModel.setAdminLoggedOut()
                },
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}