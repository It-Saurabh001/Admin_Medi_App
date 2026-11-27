package com.saurabh.mediadminapp.ui.screens

import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.ui.components.shadeRow
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.dto.Admin
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.ConfirmDialog
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.PagedLazyColumn
import com.saurabh.mediadminapp.ui.components.SectionHeader
import com.saurabh.mediadminapp.ui.components.SkeletonList

/** Admins: pending signups (approve / reject) on top, then active admins. Owner and self are protected. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminListScreen(viewModel: MyViewModel, navController: NavController) {
    val state by viewModel.getAllAdminState.collectAsStateWithLifecycle()
    val busyIds by viewModel.adminActionIds.collectAsStateWithLifecycle()
    val myId by viewModel.loggedInAdminId.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.getAllAdmin(force = true) }

    var confirmDelete by rememberSaveable { mutableStateOf<String?>(null) }
    val all = state.success?.items
    val pending = remember(all) { all.orEmpty().filter { it.isApproved == false && it.role != "deleted" } }
    val active = remember(all) { all.orEmpty().filter { it.isApproved != false && it.role != "deleted" } }

    val openAdmin: (String) -> Unit = { id -> navController.navigate(Routes.AdminFormRoutes.invoke(id)) { launchSingleTop = true } }
    AppScreenScaffold(
        title = "Admins",
        onBack = { navController.navigateUp() },
        floatingActionButton = {
            com.saurabh.mediadminapp.ui.components.GsFab(
                text = "Add admin", icon = Icons.Outlined.PersonAdd, onClick = { openAdmin(NEW_ADMIN_ID) },
            )
        },
    ) { inner ->
        PagedLazyColumn(
            isRefreshing = state.isLoading && all != null,
            onRefresh = { viewModel.getAllAdmin(force = true) },
            isLoadingMore = state.isLoadingMore,
            endReached = state.endReached,
            onLoadMore = viewModel::loadMoreAdmins,
            state = rememberLazyListState(),
            modifier = Modifier.padding(inner).fillMaxSize()
        ) {
            when {
                all == null && state.error != null -> item(key = "error", contentType = "state") {
                    ErrorState(message = state.error.orEmpty(), onRetry = { viewModel.getAllAdmin(force = true) })
                }
                all == null -> item(key = "skeleton", contentType = "state") { SkeletonList(rows = 4) }
                else -> {
                    if (pending.isNotEmpty()) {
                        item(key = "hPending", contentType = "header") { SectionHeader("Waiting for approval (${pending.size})") }
                        items(pending, key = { "p_${it.adminId}" }, contentType = { "pending" }) { a ->
                            AdminRow(a, isMe = false, busy = a.adminId in busyIds, onOpen = { openAdmin(a.adminId) }) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    com.saurabh.mediadminapp.ui.components.ShadeOutlinedButton("Reject", { confirmDelete = a.adminId }, enabled = a.adminId !in busyIds, icon = Icons.Outlined.Close, contentColor = MaterialTheme.colorScheme.error)
                                    com.saurabh.mediadminapp.ui.components.ShadeButton(
                                        text = "Approve", onClick = { viewModel.approveAdmin(a.adminId) },
                                        enabled = a.adminId !in busyIds, icon = Icons.Outlined.Check,
                                    )
                                }
                            }
                        }
                    }
                    item(key = "hActive", contentType = "header") { SectionHeader("Admins (${active.size})") }
                    if (active.isEmpty()) item(key = "empty", contentType = "state") {
                        EmptyState(title = "No admins", message = "Pull down to refresh.")
                    }
                    items(active, key = { "a_${it.adminId}" }, contentType = { "admin" }) { a ->
                        val isMe = a.adminId == myId
                        AdminRow(a, isMe = isMe, busy = a.adminId in busyIds, onOpen = { openAdmin(a.adminId) }) {
                            // Owner and your own account cannot be removed here.
                            if (a.isOwner != true && !isMe) {
                                com.saurabh.mediadminapp.ui.components.LinkButton("Remove", { confirmDelete = a.adminId }, enabled = a.adminId !in busyIds, shade = com.saurabh.mediadminapp.ui.theme.Shades.RedText)
                            }
                        }
                    }
                }
            }
        }
    }

    val target = confirmDelete?.let { id -> all?.firstOrNull { it.adminId == id } }
    if (target != null) {
        val isPending = target.isApproved == false
        ConfirmDialog(
            title = if (isPending) "Reject sign-up?" else "Remove admin?",
            message = if (isPending) "${target.name} (${target.email}) will not be able to sign in."
            else "${target.name} will lose access to the admin app. This cannot be undone here.",
            confirmText = if (isPending) "Reject" else "Remove",
            onConfirm = { viewModel.deleteAdmin(target.adminId); confirmDelete = null },
            onDismiss = { confirmDelete = null }
        )
    }
}

@Composable
private fun AdminRow(a: Admin, isMe: Boolean, busy: Boolean, onOpen: () -> Unit, actions: @Composable () -> Unit) {
    // Tappable list row (U13): White -> Green 50 / 100, Outline border, no scale, no ripple.
    Box(Modifier.fillMaxWidth().shadeRow(onOpen)) {
        Column(Modifier.padding(bottom = 8.dp)) {
            ListItem(
                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                leadingContent = { com.saurabh.mediadminapp.ui.components.PersonAvatar(a.imageUrl, a.name, size = 44.dp) },
                headlineContent = { Text(a.name + if (isMe) " (you)" else "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                supportingContent = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(a.email, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (a.isOwner == true) com.saurabh.mediadminapp.ui.components.ToneChip("Owner", com.saurabh.mediadminapp.ui.components.Tone.Info)
                            if (a.isApproved == false) com.saurabh.mediadminapp.ui.components.ToneChip("Pending", com.saurabh.mediadminapp.ui.components.Tone.Warning)
                            if (a.role == "disabled") com.saurabh.mediadminapp.ui.components.ToneChip("Disabled", com.saurabh.mediadminapp.ui.components.Tone.Danger)
                        }
                    }
                },
                trailingContent = if (busy) {
                    { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) }
                } else if (a.isApproved != false) {
                    { actions() }
                } else null
            )
            if (a.isApproved == false) {
                Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp), contentAlignment = Alignment.CenterEnd) { actions() }
            }
        }
    }
}
