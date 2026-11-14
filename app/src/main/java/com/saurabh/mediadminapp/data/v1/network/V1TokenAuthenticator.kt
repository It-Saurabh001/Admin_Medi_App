package com.saurabh.mediadminapp.data.v1.network

import com.saurabh.mediadminapp.data.v1.core.AppEvent
import com.saurabh.mediadminapp.data.v1.core.AppEvents
import com.saurabh.mediadminapp.data.v1.error.ErrorCode
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class V1TokenAuthenticator(
    private val store: V1TokenStore,
    private val refreshApi: () -> V1RefreshApi,
    private val events: AppEvents
) : Authenticator {

    private val lock = ReentrantLock()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) return null
        // Unauthenticated call (e.g. wrong login password): nothing to refresh, keep session untouched.
        val sentHeader = response.request.header("Authorization") ?: return null
        if (responseCount(response) >= 2) return fail()
        if (peekCode(response) != ErrorCode.TOKEN_EXPIRED) return fail()

        return lock.withLock {
            val sentToken = sentHeader.removePrefix("Bearer ").trim()
            val stored = store.getAccessToken()
            if (!stored.isNullOrBlank() && stored != sentToken) {
                // Another thread already rotated the tokens.
                return@withLock response.request.newBuilder().header("Authorization", "Bearer $stored").build()
            }
            val refreshToken = store.getRefreshToken()
            if (refreshToken.isNullOrBlank()) return@withLock fail()
            try {
                val result = refreshApi().refresh("Bearer $refreshToken").execute()
                val data = result.body()?.data
                when {
                    result.isSuccessful && data != null &&
                        data.accessToken.isNotBlank() && data.refreshToken.isNotBlank() -> {
                        store.saveSession(
                            accessToken = data.accessToken,
                            refreshToken = data.refreshToken,
                            role = data.role,
                            accountId = data.accountId,
                            accountType = data.accountType,
                            expiresInSeconds = data.expiresIn.toLong()
                        )
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${data.accessToken}")
                            .build()
                    }
                    result.code() == 401 || result.code() == 403 -> fail()
                    else -> null // server error: keep session, request fails with the original 401
                }
            } catch (_: IOException) {
                null // offline: keep session
            }
        }
    }

    private fun fail(): Request? {
        store.clear()
        events.emit(AppEvent.SessionInvalid)
        return null
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) { count++; prior = prior.priorResponse }
        return count
    }
}
