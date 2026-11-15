package com.saurabh.mediadminapp.ui.device

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

// ---------------------------------------------------------------------------
// Permissions (request in context only, never at startup)
// ---------------------------------------------------------------------------

enum class PermissionStatus { Granted, NotRequested, Denied, PermanentlyDenied }

class PermissionHandle internal constructor(
    val status: PermissionStatus,
    private val requestFn: () -> Unit,
    private val settingsFn: () -> Unit,
) {
    val isGranted get() = status == PermissionStatus.Granted

    /** Launches the system dialog (or nothing if already granted). */
    fun request() = requestFn()

    /** Opens this app's system settings (use for PermanentlyDenied). */
    fun openSettings() = settingsFn()
}

@Composable
fun rememberPermissionState(
    permission: String,
    onResult: (Boolean) -> Unit = {}
): PermissionHandle {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    var asked by rememberSaveable(permission) { mutableStateOf(false) }
    var tick by remember { mutableIntStateOf(0) }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            asked = true; tick++; onResult(granted)
        }
    @Suppress("UNUSED_EXPRESSION") tick
    val granted =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    val rationale = activity?.shouldShowRequestPermissionRationale(permission) == true
    val status = when {
        granted -> PermissionStatus.Granted
        !asked -> PermissionStatus.NotRequested
        rationale -> PermissionStatus.Denied
        else -> PermissionStatus.PermanentlyDenied
    }
    return PermissionHandle(
        status,
        requestFn = { if (granted) onResult(true) else launcher.launch(permission) },
        settingsFn = { context.openAppSettings() }
    )
}

fun Context.openAppSettings() {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

fun Context.hasCamera(): Boolean =
    packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

/** POST_NOTIFICATIONS exists only on API 33+; below that notifications are allowed by default. */
fun notificationPermissionOrNull(): String? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null

const val CAMERA_PERMISSION = Manifest.permission.CAMERA

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

// ---------------------------------------------------------------------------
// Connectivity (one network callback, no polling)
// ---------------------------------------------------------------------------

object ConnectivityObserver {
    fun observe(context: Context): Flow<Boolean> = callbackFlow {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        fun online(): Boolean = cm.getNetworkCapabilities(cm.activeNetwork)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(online())
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                trySend(online())
            }
        }
        trySend(online())
        val req =
            NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
        cm.registerNetworkCallback(req, cb)
        awaitClose { cm.unregisterNetworkCallback(cb) }
    }.distinctUntilChanged()
}

@Composable
fun rememberIsOnline(): State<Boolean> {
    val context = LocalContext.current.applicationContext
    val flow = remember(context) { ConnectivityObserver.observe(context) }
    return flow.collectAsState(initial = true)
}
