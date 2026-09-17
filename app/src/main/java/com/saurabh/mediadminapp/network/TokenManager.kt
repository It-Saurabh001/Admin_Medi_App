package com.saurabh.mediadminapp.network

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class TokenManager  private constructor(context: Context){

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "auth_prefs",
        Context.MODE_PRIVATE
    )

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

    // Save tokens after successful login/OTP verification
    fun saveTokens(accessToken: String, refreshToken: String, role: String, adminId: String) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putString(KEY_ADMIN_ROLE, role)
            putString(KEY_ADMIN_ID, adminId)
            apply()
        }
    }

    // Get access token
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    // Get refresh token
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    // Get user role
    fun getUserRole(): String? {
        return prefs.getString(KEY_ADMIN_ROLE, null)
    }

    // Get user ID
    fun getAdminId(): String? {
        return prefs.getString(KEY_ADMIN_ID, null)
    }
    fun saveAdminId(userId: String) {
        return prefs.edit { putString(KEY_ADMIN_ID, userId).apply() }

    }
    fun saveRole(role: String) {
        return prefs.edit { putString(KEY_ADMIN_ROLE, role).apply() }
    }

    // Update only access token (after refresh)
    fun updateAccessToken(newAccessToken: String?) {
        prefs.edit { putString(KEY_ACCESS_TOKEN, newAccessToken) }
    }

    // Clear all tokens (logout)
    fun clearTokens() {
        prefs.edit { clear().apply() }
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return !getAccessToken().isNullOrEmpty()
    }


}