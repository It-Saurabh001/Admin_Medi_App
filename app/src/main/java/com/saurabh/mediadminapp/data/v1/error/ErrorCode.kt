package com.saurabh.mediadminapp.data.v1.error

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

enum class ErrorCode {
    VALIDATION_ERROR,
    PRESCRIPTION_REQUIRED,
    UNAUTHORIZED,
    TOKEN_EXPIRED,
    TOKEN_INVALID,
    FORBIDDEN,
    ACCOUNT_BLOCKED,
    ACCOUNT_NOT_APPROVED,
    EMAIL_NOT_VERIFIED,
    EMAIL_DELIVERY_FAILED,
    NOT_FOUND,
    METHOD_NOT_ALLOWED,
    CONFLICT,
    OUT_OF_STOCK,
    PRESCRIPTION_NOT_VERIFIED,
    INVALID_TRANSITION,
    ALREADY_SOLD,
    BATCH_EXPIRED,
    PAYLOAD_TOO_LARGE,
    UNSUPPORTED_MEDIA_TYPE,
    UPGRADE_REQUIRED,
    RATE_LIMITED,
    INTERNAL_ERROR,
    UNKNOWN;

    companion object {
        fun from(raw: String?): ErrorCode =
            entries.firstOrNull { it != UNKNOWN && it.name.equals(raw?.trim(), ignoreCase = true) } ?: UNKNOWN
    }
}

class ErrorCodeAdapter : TypeAdapter<ErrorCode>() {
    override fun write(out: JsonWriter, value: ErrorCode?) {
        if (value == null) out.nullValue() else out.value(value.name)
    }

    override fun read(reader: JsonReader): ErrorCode {
        return when (reader.peek()) {
            JsonToken.NULL -> { reader.nextNull(); ErrorCode.UNKNOWN }
            JsonToken.STRING, JsonToken.NUMBER -> ErrorCode.from(reader.nextString())
            else -> { reader.skipValue(); ErrorCode.UNKNOWN }
        }
    }
}
