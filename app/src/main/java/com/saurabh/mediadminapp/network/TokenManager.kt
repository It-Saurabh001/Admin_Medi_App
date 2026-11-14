package com.saurabh.mediadminapp.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.saurabh.mediadminapp.data.v1.network.V1TokenStore
import com.saurabh.mediadminapp.utils.AppLogger


class TokenManager private constructor(context: Context) : V1TokenStore {

    // Encrypted store (falls back to the plain file if the Keystore is unavailable).
    private val prefs: SharedPreferences = createPrefs(context)

    private val _sessionExpiredEvent = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val sessionExpiredEvent: SharedFlow<Unit> = _sessionExpiredEvent.asSharedFlow()


    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val SECURE_PREFS_NAME = "auth_prefs_secure"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ADMIN_ROLE = "admin_role"
        private const val KEY_ADMIN_ID = "admin_id"
        private const val KEY_ACCOUNT_ID = "account_id"
        private const val KEY_ACCOUNT_TYPE = "account_type"
        private const val KEY_EXPIRES_AT = "expires_at"

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

        private fun createPrefs(context: Context): SharedPreferences {
            val plain = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                val secure = EncryptedSharedPreferences.create(
                    context,
                    SECURE_PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                migrate(plain, secure)
                secure
            } catch (e: Exception) {
                Log.e("TokenManager", "Encrypted prefs unavailable, using plain prefs: ${e.message}")
                plain
            }
        }

        /** One-time copy of the old plain prefs into the encrypted file, then wipe the plain file. */
        private fun migrate(plain: SharedPreferences, secure: SharedPreferences) {
            val old = plain.all
            if (old.isEmpty()) return
            val editor = secure.edit()
            old.forEach { (k, v) -> if (v is String) editor.putString(k, v) else if (v is Long) editor.putLong(k, v) }
            if (editor.commit()) plain.edit().clear().commit()
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

    fun updateTokens(newAccessToken: String, newRefreshToken: String?,role: String? = null) {
        val editor = prefs.edit()
            .putString(KEY_ACCESS_TOKEN, newAccessToken)

        if (!newRefreshToken.isNullOrBlank()) {
            editor.putString(KEY_REFRESH_TOKEN, newRefreshToken)
        }
        if (role != null) {
            editor.putString(KEY_ADMIN_ROLE, role)
        }

        val committed = editor.commit() // synchronous — must not be apply()

        if (!committed) {
            Log.e("TokenManager", "TokenManager=> updateTokens: SharedPreferences commit() returned false")
        }
    }

    /** v1 session save: both tokens + role + account identity + absolute expiry (epoch ms). */
    override fun saveSession(
        accessToken: String,
        refreshToken: String,
        role: String?,
        accountId: String?,
        accountType: String?,
        expiresInSeconds: Long?
    ) {
        val editor = prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
        if (role != null) editor.putString(KEY_ADMIN_ROLE, role)
        if (accountId != null) {
            editor.putString(KEY_ACCOUNT_ID, accountId)
            editor.putString(KEY_ADMIN_ID, accountId) // keep old getter in sync
        }
        if (accountType != null) editor.putString(KEY_ACCOUNT_TYPE, accountType)
        if (expiresInSeconds != null) {
            editor.putLong(KEY_EXPIRES_AT, System.currentTimeMillis() + expiresInSeconds * 1000L)
        }
        if (!editor.commit()) {
            Log.e("TokenManager", "TokenManager=> saveSession: commit() returned false")
        }
    }

    fun saveAdminId(adminId: String) {
        prefs.edit { putString(KEY_ADMIN_ID, adminId) }
    }

    fun saveRole(role: String) {
        prefs.edit { putString(KEY_ADMIN_ROLE, role) }
    }

    // ── Read helpers ──────────────────────────────────────────────────────────

    override fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    override fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getUserRole(): String? = prefs.getString(KEY_ADMIN_ROLE, null)

    fun getRole(): String? = prefs.getString(KEY_ADMIN_ROLE, null)

    fun getAdminId(): String? = prefs.getString(KEY_ADMIN_ID, null)

    fun getAccountId(): String? = prefs.getString(KEY_ACCOUNT_ID, null) ?: getAdminId()

    fun getAccountType(): String? = prefs.getString(KEY_ACCOUNT_TYPE, null)

    /** Access token expiry as epoch millis, or 0 if unknown. */
    fun getExpiresAt(): Long = prefs.getLong(KEY_EXPIRES_AT, 0L)

    fun isLoggedIn():Boolean {
        val loggedIn = !getAccessToken().isNullOrEmpty()
        AppLogger.d(AppLogger.TAG_AUTH,"Checking login status: $loggedIn")
        return loggedIn
    }
    // ── Eviction helpers ──────────────────────────────────────────────────────

    /** Clears everything synchronously, without emitting [sessionExpiredEvent]. */
    override fun clear() {
        prefs.edit().clear().commit()
    }

    fun clearTokens() {
        AppLogger.d(AppLogger.TAG_AUTH,"Clearing all tokens")

        prefs.edit(commit = true) { clear() }            // synchronous — must not be apply()
    }

    fun invalidateSession() {
        val editor = prefs.edit()
        editor.clear()

        val isCleared = editor.commit()
        if (isCleared) {
            // UI ko batao ki session perfectly out ho gaya hai
            val emitted = _sessionExpiredEvent.tryEmit(Unit)
            Log.w("TokenManager", "TokenManager=> invalidateSession Ab signinscreen pr jaana hai : session cleared, event emitted=$emitted")

        }
    }
}
