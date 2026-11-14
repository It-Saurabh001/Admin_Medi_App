package com.saurabh.mediadminapp.data.v1.api

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Builds text parts for @PartMap, skipping null values. */
fun textParts(vararg pairs: Pair<String, Any?>): Map<String, RequestBody> =
    pairs.filter { it.second != null }
        .associate { (k, v) -> k to v.toString().toRequestBody("text/plain".toMediaType()) }
