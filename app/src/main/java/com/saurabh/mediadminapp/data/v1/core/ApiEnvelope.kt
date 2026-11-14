package com.saurabh.mediadminapp.data.v1.core

import com.google.gson.JsonObject

data class ApiEnvelope<T>(
    val status: Int,
    val message: String,
    val data: T?
)

data class Pagination(
    val limit: Int,
    val offset: Int,
    val total: Int
)

data class PagedEnvelope<T>(
    val status: Int,
    val message: String,
    val data: List<T>,
    val pagination: Pagination,
    val summary: JsonObject? = null // unverified: reports/sales only
)
