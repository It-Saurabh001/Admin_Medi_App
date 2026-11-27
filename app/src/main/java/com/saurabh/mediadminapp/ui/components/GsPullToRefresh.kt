package com.saurabh.mediadminapp.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

/** Time allowed for a refresh to report "loading" before the indicator is dismissed. */
private const val SETTLE_MS = 400L

/**
 * Pull-to-refresh whose spinner appears only for a refresh the user pulled.
 *
 * Screens reload their data when they are opened or returned to; passing that loading flag straight to
 * `PullToRefreshBox` dropped the spinner on every back navigation. Here those reloads stay silent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GsPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    var pulled by remember { mutableStateOf(false) }
    LaunchedEffect(pulled, isRefreshing) {
        if (pulled && !isRefreshing) {
            delay(SETTLE_MS)
            pulled = false
        }
    }
    PullToRefreshBox(
        isRefreshing = pulled,
        onRefresh = {
            pulled = true
            onRefresh()
        },
        modifier = modifier,
        content = content,
    )
}
