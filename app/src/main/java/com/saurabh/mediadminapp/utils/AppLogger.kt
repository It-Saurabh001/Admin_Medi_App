package com.saurabh.mediadminapp.utils

import android.util.Log


/**
 * Centralized Logger for MediAdminApp.
 * Provides consistent tagging and formatting for all application layers.
 */
object AppLogger {
    private const val BASE_TAG = "MediAdminApp"

    // Layer-specific tags
    const val TAG_NETWORK = "$BASE_TAG-Network"
    const val TAG_VIEWMODEL = "$BASE_TAG-ViewModel"
    const val TAG_UI = "$BASE_TAG-UI"
    const val TAG_AUTH = "$BASE_TAG-Auth"
    const val TAG_OTP = "$BASE_TAG-OTP"
    const val TAG_NAV = "$BASE_TAG-Nav"

    fun d(tag: String, message: String) = Log.d(tag, message)
    fun i(tag: String, message: String) = Log.i(tag, message)
    fun w(tag: String, message: String) = Log.w(tag, message)
    fun e(tag: String, message: String, throwable: Throwable? = null) = Log.e(tag, message, throwable)
    fun v(tag: String, message: String) = Log.v(tag, message)

    /**
     * Logs network request details.
     */
    fun logRequest(method: String, url: String, headers: Map<String, String>, body: String?) {
        val sanitizedHeaders = headers.filterKeys { it.lowercase() != "authorization" }
        d(TAG_NETWORK, "--> SENDING $method $url")
        d(TAG_NETWORK, "Headers: $sanitizedHeaders")
        body?.let { d(TAG_NETWORK, "Body: ${it.take(500)}") }
    }

    /**
     * Logs network response details.
     */
    fun logResponse(code: Int, url: String, durationMs: Long, body: String?) {
        d(TAG_NETWORK, "<-- RECEIVED $code for $url (${durationMs}ms)")
        body?.let { d(TAG_NETWORK, "Body: ${it.take(200)}...") }
    }
}
