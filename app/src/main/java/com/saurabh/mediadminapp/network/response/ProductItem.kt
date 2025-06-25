package com.saurabh.mediadminapp.network.response

data class ProductItem(
    val Product_id: String,
    val category: String,
    val id: Int,
    val name: String,
    val price: Double,
    val stock: Int
)