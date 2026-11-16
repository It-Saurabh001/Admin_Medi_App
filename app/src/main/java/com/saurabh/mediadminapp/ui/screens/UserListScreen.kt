package com.saurabh.mediadminapp.ui.screens

import com.saurabh.mediadminapp.ui.components.GsFilterChip
import com.saurabh.mediadminapp.ui.components.ChipLabel
import com.saurabh.mediadminapp.ui.components.GsTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.saurabh.mediadminapp.MyViewModel
import com.saurabh.mediadminapp.data.v1.dto.User
import com.saurabh.mediadminapp.ui.components.AppScreenScaffold
import com.saurabh.mediadminapp.ui.components.EmptyState
import com.saurabh.mediadminapp.ui.components.ErrorState
import com.saurabh.mediadminapp.ui.components.PagedLazyColumn
import com.saurabh.mediadminapp.ui.components.PersonAvatar
import com.saurabh.mediadminapp.ui.components.SkeletonList
import com.saurabh.mediadminapp.ui.components.Tone
import com.saurabh.mediadminapp.ui.components.ToneChip
import com.saurabh.mediadminapp.ui.components.shadeRow
import com.saurabh.mediadminapp.ui.screens.nav.Routes
import com.saurabh.mediadminapp.ui.theme.GS

private enum class UserFilter(val label: String) { ALL("All"), PENDING("Pending"), APPROVED("Approved"), BLOCKED("Blocked") }

/** Customers list: search + status chips (applied to loaded pages), pull-to-refresh, load-more. Tap -> UserDetails. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    viewModel: MyViewModel,
    navController: NavController,
    onMenuClick: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
) {
    val state by viewModel.getAllUserState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.getAllUsers() }
    val focus = LocalFocusManager.current
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(UserFilter.ALL) }

    val all = state.success?.items
    val shown = remember(all, query, filter) {
        all.orEmpty().filter { u ->
            val f = when (filter) {
                UserFilter.ALL -> true
                UserFilter.PENDING -> !u.isApproved && !u.isBlocked
                UserFilter.APPROVED -> u.isApproved && !u.isBlocked
                UserFilter.BLOCKED -> u.isBlocked
            }
            f && (query.isBlank() || u.name.contains(query, true) || u.email.contains(query, true) ||
                u.phoneNumber.orEmpty().contains(query) || u.userId.contains(query, true))
        }
    }

    AppScreenScaffold(
        title = "Customers",
        onBack = if (onMenuClick == null) ({ navController.popBackStack(); Unit }) else null,
        navigationIcon = onMenuClick?.let { open -> { com.saurabh.mediadminapp.ui.components.TileIconButton(Icons.Outlined.Menu, "Menu", open, Modifier.padding(start = 4.dp)) } },
        scrollBehavior = scrollBehavior,
    ) { inner ->
        PagedLazyColumn(
            isRefreshing = state.isLoading && all != null,
            onRefresh = { viewModel.getAllUsers(force = true) },
            isLoadingMore = state.isLoadingMore,
            endReached = state.endReached,
            onLoadMore = viewModel::loadMoreUsers,
            state = rememberLazyListState(),
            modifier = Modifier.padding(inner).fillMaxSize(),
        ) {
            item(key = "search", contentType = "search") {
                GsTextField(
                    value = query, onValueChange = { query = it },
                    placeholder = { Text("Search name, email, phone or id") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = if (query.isNotEmpty()) {
                        { IconButton(onClick = { query = "" }) { Icon(Icons.Outlined.Close, contentDescription = "Clear search") } }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, autoCorrectEnabled = false, imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item(key = "chips", contentType = "chips") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(UserFilter.entries, key = { it.name }) { f ->
                        GsFilterChip(
                            selected = filter == f,
                            onClick = { filter = f },
                            label = { ChipLabel(f.label) },
                        )
                    }
                }
            }
            when {
                all == null && state.error != null -> item(key = "error", contentType = "state") {
                    ErrorState(message = state.error.orEmpty(), onRetry = { viewModel.getAllUsers(force = true) })
                }
                all == null -> item(key = "skeleton", contentType = "state") { SkeletonList(rows = 6) }
                shown.isEmpty() -> item(key = "empty", contentType = "state") {
                    EmptyState(
                        title = if (all.isEmpty()) "No customers yet" else "No matching customers",
                        message = if (all.isEmpty()) "Registered customers appear here." else "Try another search or filter.",
                    )
                }
                else -> items(shown, key = { it.userId }, contentType = { "user" }) { u ->
                    UserRow(u) { navController.navigate(Routes.UserDetailsRoutes.invoke(u.userId)) { launchSingleTop = true } }
                }
            }
        }
    }
}

@Composable
private fun UserRow(u: User, onClick: () -> Unit) {
    ListItem(
        leadingContent = { PersonAvatar(u.imageUrl, u.name, size = 48.dp) },
        headlineContent = { Text(u.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    listOfNotNull(u.email.takeIf { it.isNotBlank() }, u.phoneNumber?.takeIf { it.isNotBlank() }).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    when {
                        u.isBlocked -> ToneChip("Blocked", Tone.Danger)
                        u.isApproved -> ToneChip("Approved", Tone.Success)
                        else -> ToneChip("Pending", Tone.Warning)
                    }
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth().shadeRow(onClick),
    )
}
