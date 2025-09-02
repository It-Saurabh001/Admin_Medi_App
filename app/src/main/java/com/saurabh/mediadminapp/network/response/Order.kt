package com.saurabh.mediadminapp.network.response

import com.google.gson.annotations.SerializedName

data class Order(
    val category: String,
    val date_of_order_creation: String,
    val id: Int,
    @SerializedName("isApproved")
    var _isApproved: Any? = null,
    val message: String,
    val order_id: String,
    val price: Double,
    val product_id: String,
    val product_name: String,
    val quantity: Int,
    val total_amount: Double,
    val user_id: String,
    val user_name: String
){
    val isApproved: Boolean         // This is a custom getter to handle both Boolean and Number types of approval
        get() = when(_isApproved){
            is Boolean -> _isApproved as Boolean
            is Number -> (_isApproved as Number).toInt() == 1
            else -> false
        }


}