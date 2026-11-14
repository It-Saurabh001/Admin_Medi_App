package com.saurabh.mediadminapp.data.v1.core

/** GET /health (not enveloped). */
data class HealthResponse(
    val status: String? = null,
    val service: String? = null
)

/** GET /health/ready (not enveloped). `checks` keys: database, cache. */
data class HealthReadyResponse(
    val status: String? = null,
    val service: String? = null,
    val checks: Map<String, String>? = null
)
