package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.R
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveContent
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveWidths
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.DetailCard
import com.saurabh.mediadminapp.ui.components.InfoTile
import com.saurabh.mediadminapp.ui.components.Tone
import com.saurabh.mediadminapp.ui.components.ToneChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(viewModel: MyViewModel, navController: NavController) {
    val adminId by viewModel.loggedInAdminId.collectAsState()
    val usersState by viewModel.getAllUserState.collectAsState()
    val productState by viewModel.getAllProduct.collectAsState()
    val context = LocalContext.current
    val version = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "1.0"
    }

    LaunchedEffect(Unit) {
        if (usersState.success == null) viewModel.getAllUsers()
        if (productState.success == null) viewModel.getAllProduct()
    }

    AppScreenScaffold(title = "About", onBack = { navController.popBackStack() }) { padding ->
        AdaptiveContent(Modifier.padding(padding), maxWidth = AdaptiveWidths.Form) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            ) {
                item(key = "hero") {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Image(painterResource(R.drawable.ic_logo), contentDescription = "App logo", contentScale = ContentScale.Fit, modifier = Modifier.size(96.dp))
                        Text("Medi Admin", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
                        ToneChip("Version $version", Tone.Info)
                    }
                }
                item(key = "counts") {
                    DetailCard(title = "Loaded data", icon = Icons.Outlined.Info) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            InfoTile("Customers", "${usersState.success?.items?.size ?: 0}", Modifier.weight(1f), Icons.Outlined.People)
                            InfoTile("Products", "${productState.success?.items?.size ?: 0}", Modifier.weight(1f), Icons.Outlined.Inventory2)
                        }
                    }
                }
                item(key = "about") {
                    DetailCard(title = "About this app", icon = Icons.Outlined.Info) {
                        Text(
                            "Admin app for the pharmacy: manage products and stock batches, approve and sell orders, review sales and reports, and manage customers and admins.",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        InfoTile("Signed in as", adminId ?: "Not signed in", icon = Icons.Outlined.Fingerprint)
                    }
                }
            }
        }
    }
}
