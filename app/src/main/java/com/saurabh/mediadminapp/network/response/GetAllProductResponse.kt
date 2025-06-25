package com.saurabh.mediadminapp.network.response

data class GetAllProductResponse(
    val message: String,
    val products: List<ProductItem>,
    val status: Int
)