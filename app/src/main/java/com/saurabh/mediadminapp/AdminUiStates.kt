package com.saurabh.mediadminapp

import com.saurabh.mediadminapp.data.v1.dto.Admin
import com.saurabh.mediadminapp.data.v1.error.AppError

/** Signup email OTP: verify (verified=true) and resend (resent=true). */
data class EmailVerifyState(
    val isLoading: Boolean = false,
    val verified: Boolean = false,
    val resent: Boolean = false,
    val error: String? = null,
    val appError: AppError? = null,
)

/** POST /admins/register. success.isApproved == false -> "awaiting approval". */
data class RegisterAdminState(
    val isLoading: Boolean = false,
    val success: Admin? = null,
    val error: String? = null,
    val appError: AppError? = null,
)

/** Orders of one checkout (GET orders?checkout_id=) and the last group action result. */
data class CheckoutGroupState(
    val isLoading: Boolean = false,
    val orders: List<com.saurabh.mediadminapp.data.v1.dto.Order> = emptyList(),
    val total: Int = 0,
    val error: String? = null,
    val actionRunning: Boolean = false,
    val actionSummary: String? = null,
)
