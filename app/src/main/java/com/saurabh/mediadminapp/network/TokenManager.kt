package com.saurabh.mediadminapp.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * TokenManager — single source of truth for all auth tokens.
 *
 * Design decisions:
 * ─────────────────
 * 1. All writes that must be visible to the immediately-following network call
 *    use [SharedPreferences.Editor.commit] (synchronous disk write) rather than
 *    the asynchronous [SharedPreferences.Editor.apply].  If we used apply() and
 *    the OkHttp thread read the prefs before the disk write finished, the new
 *    access token would be invisible and the retried request would send the
 *    expired token again — causing a second 401 and an unrecoverable loop.
 *
 * 2. [invalidateSession] both wipes tokens AND emits on [sessionExpiredEvent] so
 *    the Compose UI can react (navigate to Login) without the ViewModel having to
 *    poll.  [MutableSharedFlow.tryEmit] is thread-safe and does not require a
 *    coroutine context, making it safe to call from OkHttp's background threads.
 */
class TokenManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // ── Session-expiry event ──────────────────────────────────────────────────
    // replay = 0  → late subscribers (e.g. recomposed NavHost) don't receive a
    //               stale eviction signal from a previous session.
    // extraBufferCapacity = 1  → tryEmit() from a non-coroutine thread always
    //                            succeeds (buffer absorbs the event even if no
    //                            collector is currently active).
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
                instance ?: TokenManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    // ── Write helpers ─────────────────────────────────────────────────────────

    /**
     * Persist all tokens after a successful login / OTP verification.
     * Uses commit() so the access token is readable by any OkHttp thread that
     * immediately fires after this call returns.
     */
    fun saveTokens(accessToken: String, refreshToken: String, role: String, adminId: String) {
        val committed = prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_ADMIN_ROLE, role)
            .putString(KEY_ADMIN_ID, adminId)
            .commit()                             // synchronous — must not be apply()

        if (!committed) {
            Log.e("TokenManager", "saveTokens: SharedPreferences commit() returned false")
        }
    }

    /**
     * Overwrite only the access token after a silent token refresh.
     * commit() is mandatory here: the TokenAuthenticator calls this on an OkHttp
     * thread and then immediately retries the request — apply() would create a
     * race where the retried request reads the old token from the in-memory cache
     * before the async disk write completes.
     */
    fun updateAccessToken(newAccessToken: String) {
        val committed = prefs.edit()
            .putString(KEY_ACCESS_TOKEN, newAccessToken)
            .commit()                             // synchronous — must not be apply()

        if (!committed) {
            Log.e("TokenManager", "updateAccessToken: SharedPreferences commit() returned false")
        }
    }

    fun saveAdminId(adminId: String) {
        prefs.edit().putString(KEY_ADMIN_ID, adminId).apply()
    }

    fun saveRole(role: String) {
        prefs.edit().putString(KEY_ADMIN_ROLE, role).apply()
    }

    // ── Read helpers ──────────────────────────────────────────────────────────

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getUserRole(): String? = prefs.getString(KEY_ADMIN_ROLE, null)

    fun getAdminId(): String? = prefs.getString(KEY_ADMIN_ID, null)

    fun isLoggedIn(): Boolean = !getAccessToken().isNullOrEmpty()

    // ── Eviction helpers ──────────────────────────────────────────────────────

    /**
     * Wipe all credentials from disk.
     * Used during user-initiated logout only; does NOT emit [sessionExpiredEvent].
     */
    fun clearTokens() {
        prefs.edit().clear().commit()            // synchronous — must not be apply()
    }

    /**
     * Wipe all credentials AND signal every active UI collector that the session
     * has been forcibly terminated (e.g. refresh token rejected by the server).
     *
     * Safe to call from any thread (OkHttp dispatcher, Main, IO, etc.):
     *   • clearTokens() only touches SharedPreferences — no thread restriction.
     *   • tryEmit() on a SharedFlow is thread-safe per Kotlin Flows spec.
     */
    fun invalidateSession() {
        clearTokens()
        val emitted = _sessionExpiredEvent.tryEmit(Unit)
        Log.w("TokenManager", "invalidateSession: session cleared, event emitted=$emitted")
    }
}