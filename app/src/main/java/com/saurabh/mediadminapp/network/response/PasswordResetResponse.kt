package com.saurabh.mediadminapp.network.response

import com.google.gson.annotations.SerializedName

data class PasswordResetResponse(
    val message: String,
    val status: Int,
    @SerializedName("user_id") val userId: String? = null
)