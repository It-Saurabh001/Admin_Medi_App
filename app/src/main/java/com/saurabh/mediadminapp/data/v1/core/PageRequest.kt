package com.saurabh.mediadminapp.data.v1.core

data class PageRequest(
    val limit: Int = 20,
    val offset: Int = 0,
    val sort: String? = null,
    val order: String? = null
) {
    fun toQueryMap(): Map<String, String> = buildMap {
        put("limit", limit.coerceIn(1, 100).toString())
        put("offset", offset.coerceAtLeast(0).toString())
        sort?.takeIf { it.isNotBlank() }?.let { put("sort", it) }
        order?.takeIf { it.isNotBlank() }?.let { put("order", it) }
    }
}

/** Next page (offset += limit) while offset < total, else null. */
fun PageRequest.loadMore(pagination: Pagination): PageRequest? {
    val next = offset + limit
    return if (next < pagination.total) copy(offset = next) else null
}
