package com.saurabh.mediadminapp.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import androidx.core.content.edit
import androidx.security.crypto.MasterKey


class TokenManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _sessionExpiredEvent = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val sessionExpiredEvent: SharedFlow<Unit> = _sessionExpiredEvent.asSharedFlow()


    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ADMIN_ROLE = "admin_role"
        private const val KEY_ADMIN_ID = "admin_id"

        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                Log.d("PERF_TRACE", "TokenManager => -------------------------------------------------------------------------------")
                instance ?: TokenManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    fun saveTokens(accessToken: String, refreshToken: String, role: String, adminId: String) {
        val committed = prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_ADMIN_ROLE, role)
            .putString(KEY_ADMIN_ID, adminId)
            .commit()                             // synchronous — must not be apply()

        if (!committed) {
            Log.e("TokenManager", " TokenManager=> saveTokens: SharedPreferences commit() returned false")
        }
    }

    fun updateTokens(newAccessToken: String, newRefreshToken: String?) {
        val editor = prefs.edit()
            .putString(KEY_ACCESS_TOKEN, newAccessToken)
        
        if (!newRefreshToken.isNullOrBlank()) {
            editor.putString(KEY_REFRESH_TOKEN, newRefreshToken)
        }
        
        val committed = editor.commit() // synchronous — must not be apply()

        if (!committed) {
            Log.e("TokenManager", "TokenManager=> updateTokens: SharedPreferences commit() returned false")
        }
    }

    fun saveAdminId(adminId: String) {
        prefs.edit { putString(KEY_ADMIN_ID, adminId) }
    }

    fun saveRole(role: String) {
        prefs.edit { putString(KEY_ADMIN_ROLE, role) }
    }

    // ── Read helpers ──────────────────────────────────────────────────────────

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getUserRole(): String? = prefs.getString(KEY_ADMIN_ROLE, null)

    fun getAdminId(): String? = prefs.getString(KEY_ADMIN_ID, null)

    fun isLoggedIn(): Boolean = !getAccessToken().isNullOrEmpty()

    // ── Eviction helpers ──────────────────────────────────────────────────────

    fun clearTokens() {
        prefs.edit(commit = true) { clear() }            // synchronous — must not be apply()
    }

    fun invalidateSession() {
        clearTokens()
        val emitted = _sessionExpiredEvent.tryEmit(Unit)
        Log.w("TokenManager", "TokenManager=> invalidateSession: session cleared, event emitted=$emitted")
    }
}