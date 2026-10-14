package com.saurabh.mediadminapp.ui.auth.data

import kotlinx.coroutines.delay
import kotlin.random.Random

// ─── Result wrapper ───────────────────────────────────────────────────────────
sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

// ─── Auth Repository interface ────────────────────────────────────────────────
interface AuthRepository {
    suspend fun login(email: String, password: String): AuthResult<Unit>
    suspend fun verifyOtp(email: String, code: String): AuthResult<Unit>
    suspend fun resendOtp(email: String): AuthResult<Unit>
    suspend fun requestPasswordReset(email: String): AuthResult<Unit>
    suspend fun resetPassword(email: String, code: String, newPassword: String): AuthResult<Unit>
    suspend fun hasValidSession(): Boolean
}

// ─── Fake / Mock implementation ───────────────────────────────────────────────
class FakeAuthRepository : AuthRepository {

    private val validEmail = "admin@curapharmacy.in"
    private val validPassword = "Admin@123"
    private val validOtp = "481927"

    private suspend fun randomDelay() = delay(Random.nextLong(800, 1200))

    override suspend fun login(email: String, password: String): AuthResult<Unit> {
        randomDelay()
        return if (email == validEmail && password == validPassword) {
            AuthResult.Success(Unit)
        } else {
            AuthResult.Error("INVALID_CREDENTIALS")
        }
    }

    override suspend fun verifyOtp(email: String, code: String): AuthResult<Unit> {
        randomDelay()
        return if (code == validOtp) {
            AuthResult.Success(Unit)
        } else {
            AuthResult.Error("INVALID_OTP")
        }
    }

    override suspend fun resendOtp(email: String): AuthResult<Unit> {
        randomDelay()
        return AuthResult.Success(Unit)
    }

    override suspend fun requestPasswordReset(email: String): AuthResult<Unit> {
        randomDelay()
        return if (email == validEmail) {
            AuthResult.Success(Unit)
        } else {
            AuthResult.Error("EMAIL_NOT_FOUND")
        }
    }

    override suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String
    ): AuthResult<Unit> {
        randomDelay()
        return if (code == validOtp) {
            AuthResult.Success(Unit)
        } else {
            AuthResult.Error("INVALID_OTP")
        }
    }

    override suspend fun hasValidSession(): Boolean {
        delay(300)
        return false // No persisted session in mock
    }
}
