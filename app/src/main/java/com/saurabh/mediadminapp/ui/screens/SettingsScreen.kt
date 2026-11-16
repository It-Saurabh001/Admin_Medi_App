package com.saurabh.mediadminapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveContent
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveWidths
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.ConfirmDialog
import com.saurabh.mediadminapp.ui.components.DetailCard
import com.saurabh.mediadminapp.ui.components.InfoTile
import com.saurabh.mediadminapp.ui.components.NavRow
import com.saurabh.mediadminapp.ui.components.ShadeOutlinedButton
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.ui.theme.GS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MyViewModel, navController: NavController) {
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    val go: (String) -> Unit = { route -> navController.navigate(route) { launchSingleTop = true } }

    AppScreenScaffold(title = "Settings", onBack = { navController.popBackStack() }) { padding ->
        AdaptiveContent(Modifier.padding(padding), maxWidth = AdaptiveWidths.Form) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            ) {
                item(key = "account") {
                    DetailCard(title = "Account", icon = Icons.Outlined.Person) {
                        NavRow("My profile", { go(Routes.ProfileRoutes.route) }, subtitle = "Name, email and admin ID", icon = Icons.Outlined.Person)
                        NavRow("Admins", { go(Routes.AdminListRoutes.route) }, subtitle = "Approve signups and manage admins", icon = Icons.Outlined.AdminPanelSettings)
                    }
                }
                item(key = "reports") {
                    DetailCard(title = "Reports", icon = Icons.Outlined.BarChart) {
                        NavRow("Sales reports", { go(Routes.ReportsRoutes.route) }, subtitle = "Revenue by day or product", icon = Icons.Outlined.BarChart)
                        NavRow("Top products", { go(Routes.TopProductsRoutes.route) }, subtitle = "Best sellers for a date range", icon = Icons.Outlined.Star)
                    }
                }
                item(key = "app") {
                    DetailCard(title = "App", icon = Icons.Outlined.Info) {
                        InfoTile("Appearance", "Follows your device light / dark setting", icon = Icons.Outlined.DarkMode)
                        NavRow("About", { go(Routes.AboutRoutes.route) }, subtitle = "Version and app details", icon = Icons.Outlined.Info)
                    }
                }
                item(key = "logout") {
                    ShadeOutlinedButton(
                        text = "Log out", onClick = { confirmLogout = true },
                        icon = Icons.AutoMirrored.Outlined.Logout, contentColor = GS.Red600,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    if (confirmLogout) {
        ConfirmDialog(
            title = "Log out?",
            message = "You will need to sign in again to manage orders and stock.",
            confirmText = "Log out",
            onConfirm = { confirmLogout = false; viewModel.setAdminLoggedOut() },
            onDismiss = { confirmLogout = false },
        )
    }
}
