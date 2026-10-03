package com.saurabh.mediadminapp.network.response

import com.google.gson.annotations.SerializedName

data class UserItem(
    val address: String? = null,
    @SerializedName("block")
     val _block: Any? = null,
    val date_of_account_creation: String,
    val email: String? = null,
    val id: Int,
    @SerializedName("isApproved")
    val _isApproved: Any? = null,
    val name: String,
    val password: String? = null,
    val phone_number: String? = null,
    val pin_code: String? = null,
    val role: String,
    val user_id: String
){
    val isApproved: Boolean         // This is a custom getter to handle both Boolean and Number types of approval
        get() = when(_isApproved){
            is Boolean -> _isApproved as Boolean
            is Number -> (_isApproved as Number).toInt() == 1
            else -> false
        }
    val block: Boolean
        get() = when(_block){
            is Boolean -> _block as Boolean
            is Number -> (_block as Number).toInt() == 1
            else -> false
        }
}

