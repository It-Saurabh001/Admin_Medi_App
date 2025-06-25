package com.saurabh.mediadminapp.network.response

data class GetSpecificProductResponse(
    val message: String,
    val product: ProductItem,
    val status: Int
)