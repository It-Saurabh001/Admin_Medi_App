package com.saurabh.mediadminapp.data.v1.dto

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import java.time.Instant

data class AuditLog(
    @SerializedName("audit_id") val auditId: String,
    @SerializedName("actor_id") val actorId: String,
    @SerializedName("actor_role") val actorRole: String,
    val action: String,
    val entity: String,
    @SerializedName("entity_id") val entityId: String,
    val details: JsonObject?, // free-form per action; unverified shape
    @SerializedName("created_at") val createdAt: Instant?
)
