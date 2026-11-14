package com.saurabh.mediadminapp.data.v1.core

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

enum class OrderStatus(val wire: String) {
    PENDING("pending"),
    APPROVED("approved"),
    SOLD("sold"),
    CANCELLED("cancelled"),
    RETURNED("returned"),
    UNKNOWN("unknown");

    companion object {
        fun fromWire(raw: String?): OrderStatus =
            entries.firstOrNull { it != UNKNOWN && it.wire.equals(raw?.trim(), ignoreCase = true) } ?: UNKNOWN
    }
}

class OrderStatusAdapter : TypeAdapter<OrderStatus>() {
    override fun write(out: JsonWriter, value: OrderStatus?) {
        if (value == null) out.nullValue() else out.value(value.wire)
    }

    override fun read(reader: JsonReader): OrderStatus {
        return when (reader.peek()) {
            JsonToken.NULL -> { reader.nextNull(); OrderStatus.UNKNOWN }
            JsonToken.STRING -> OrderStatus.fromWire(reader.nextString())
            else -> { reader.skipValue(); OrderStatus.UNKNOWN }
        }
    }
}
