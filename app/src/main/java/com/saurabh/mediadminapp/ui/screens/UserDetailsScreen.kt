package com.saurabh.mediadminapp.ui.screens

import com.saurabh.mediadminapp.ui.components.GsPullToRefreshBox
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.dto.User as UserItem
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveContent
import com.saurabh.mediadminapp.ui.adaptive.AdaptiveWidths
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.ConfirmDialog
import com.saurabh.mediadminapp.ui.components.DetailCard
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.InfoTile
import com.saurabh.mediadminapp.ui.components.LoadingState
import com.saurabh.mediadminapp.ui.components.PersonAvatar
import com.saurabh.mediadminapp.ui.components.ShadeButton
import com.saurabh.mediadminapp.ui.components.ShadeOutlinedButton
import com.saurabh.mediadminapp.ui.components.Tone
import com.saurabh.mediadminapp.ui.components.ToneChip
import com.saurabh.mediadminapp.ui.components.dateTimeLabel
import com.saurabh.mediadminapp.ui.screens.nav.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailsScreen(user_id: String, viewModel: MyViewModel, navController: NavController) {
    val usersState by viewModel.getAllUserState.collectAsState()
    val deleteState by viewModel.deleteUserState.collectAsState()
    val statusStates by viewModel.isApprovedUser.collectAsState()

    val user = usersState.success?.items?.find { it.userId == user_id }
    val statusBusy = statusStates[user_id]?.isLoading == true

    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var confirmBlock by rememberSaveable { mutableStateOf(false) }

    // (Re)load when nothing is cached, e.g. after a failed delete reset the list.
    LaunchedEffect(usersState.success == null) {
        if (usersState.success == null && !usersState.isLoading) viewModel.getAllUsers()
    }

    LaunchedEffect(deleteState.success) {
        if (deleteState.success != null && !deleteState.isLoading && deleteState.error == null) {
            viewModel.resetDeleteUserState()
            navController.navigate(Routes.HomeRoutes()) {
                popUpTo(Routes.HomeRoutes()) { inclusive = true }
            }
        }
    }
    LaunchedEffect(deleteState.error) {
        if (deleteState.error != null && !deleteState.isLoading) viewModel.resetDeleteUserState()
    }

    AppScreenScaffold(
        title = "User details",
        onBack = { navController.popBackStack() },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                usersState.isLoading && usersState.success == null -> LoadingState()
                usersState.error != null && usersState.success == null -> ErrorState(
                    message = usersState.error ?: "Could not load users",
                    onRetry = { viewModel.getAllUsers(force = true) },
                )
                user == null -> EmptyState(
                    title = "User not found",
                    message = "This user may have been deleted.",
                )
                else -> GsPullToRefreshBox(
                    isRefreshing = usersState.isLoading,
                    onRefresh = { viewModel.getAllUsers(force = true) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    AdaptiveContent(maxWidth = AdaptiveWidths.Form) {
                        UserDetailsHub(
                            user = user,
                            busy = statusBusy,
                            isDeleting = deleteState.isLoading,
                            onApprove = { viewModel.isApprovedUser(user.userId, true) },
                            onUnblock = { viewModel.unblockUser(user.userId) },
                            onBlock = { confirmBlock = true },
                            onDelete = { confirmDelete = true },
                        )
                    }
                }
            }
        }
    }

    if (confirmBlock && user != null) {
        ConfirmDialog(
            title = "Block user?",
            message = "${user.name} will no longer be able to place or view orders.",
            confirmText = "Block",
            onConfirm = { confirmBlock = false; viewModel.isApprovedUser(user.userId, false) },
            onDismiss = { confirmBlock = false },
        )
    }
    if (confirmDelete && user != null) {
        ConfirmDialog(
            title = "Delete user?",
            message = "${user.name} will be removed. This cannot be undone.",
            confirmText = "Delete",
            onConfirm = { confirmDelete = false; viewModel.deleteUser(user.userId) },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun UserDetailsHub(
    user: UserItem,
    busy: Boolean,
    isDeleting: Boolean,
    onApprove: () -> Unit,
    onUnblock: () -> Unit,
    onBlock: () -> Unit,
    onDelete: () -> Unit,
) {
    val none = "Not provided"
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
    ) {
        item(key = "identity") {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PersonAvatar(url = user.imageUrl, name = user.name, size = 96.dp)
                Text(
                    user.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ToneChip(if (user.isApproved) "Approved" else "Pending approval", if (user.isApproved) Tone.Success else Tone.Warning)
                    if (user.isBlocked) ToneChip("Blocked", Tone.Danger)
                }
            }
        }

        item(key = "account") {
            DetailCard(title = "Account", icon = Icons.Outlined.Person) {
                InfoTile("Client ID", user.userId, icon = Icons.Outlined.Badge)
                InfoTile("Registered", user.createdAt.dateTimeLabel(), icon = Icons.Outlined.CalendarMonth)
            }
        }

        item(key = "contact") {
            DetailCard(title = "Contact & address", icon = Icons.Outlined.Phone) {
                InfoTile("Phone", user.phoneNumber.orEmpty().ifBlank { none }, icon = Icons.Outlined.Phone)
                InfoTile("Email", user.email.ifBlank { none }, icon = Icons.Outlined.Email)
                InfoTile("Address", user.address.orEmpty().ifBlank { none }, icon = Icons.Outlined.Home)
                InfoTile("Pincode", user.pinCode.orEmpty().ifBlank { none }, icon = Icons.Outlined.LocationOn)
            }
        }

        item(key = "actions") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when {
                    user.isBlocked -> ShadeButton("Unblock user", onUnblock, Modifier.fillMaxWidth(), isLoading = busy)
                    !user.isApproved -> ShadeButton("Approve user", onApprove, Modifier.fillMaxWidth(), isLoading = busy)
                }
                if (!user.isBlocked) {
                    ShadeOutlinedButton("Block user", onBlock, Modifier.fillMaxWidth(), enabled = !busy && !isDeleting, contentColor = MaterialTheme.colorScheme.error)
                }
                ShadeOutlinedButton("Delete user", onDelete, Modifier.fillMaxWidth(), enabled = !isDeleting, contentColor = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun UserDetailsScreen1(user_id: String, viewModel: MyViewModel, navController: NavController) {
    UserDetailsScreen(user_id = user_id, viewModel = viewModel, navController = navController)
}
