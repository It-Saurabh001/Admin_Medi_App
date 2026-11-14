package com.saurabh.mediadminapp.data.v1.core

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.saurabh.mediadminapp.data.v1.error.ErrorCode
import com.saurabh.mediadminapp.data.v1.error.ErrorCodeAdapter
import java.time.Instant
import java.time.LocalDate

/** ISO-8601 UTC ("2026-11-14T17:33:47Z") <-> Instant. Unparseable -> null. */
class InstantAdapter : TypeAdapter<Instant>() {
    override fun write(out: JsonWriter, value: Instant?) {
        if (value == null) out.nullValue() else out.value(value.toString())
    }

    override fun read(reader: JsonReader): Instant? {
        if (reader.peek() == JsonToken.NULL) { reader.nextNull(); return null }
        if (reader.peek() != JsonToken.STRING) { reader.skipValue(); return null }
        return try { Instant.parse(reader.nextString()) } catch (_: Exception) { null }
    }
}

/** "YYYY-MM-DD" <-> LocalDate. Unparseable -> null. */
class LocalDateAdapter : TypeAdapter<LocalDate>() {
    override fun write(out: JsonWriter, value: LocalDate?) {
        if (value == null) out.nullValue() else out.value(value.toString())
    }

    override fun read(reader: JsonReader): LocalDate? {
        if (reader.peek() == JsonToken.NULL) { reader.nextNull(); return null }
        if (reader.peek() != JsonToken.STRING) { reader.skipValue(); return null }
        return try { LocalDate.parse(reader.nextString()) } catch (_: Exception) { null }
    }
}

object V1Gson {
    fun builder(): GsonBuilder = GsonBuilder()
        .registerTypeAdapter(Instant::class.java, InstantAdapter().nullSafe())
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter().nullSafe())
        .registerTypeAdapter(OrderStatus::class.java, OrderStatusAdapter())
        .registerTypeAdapter(ErrorCode::class.java, ErrorCodeAdapter())

    val instance: Gson by lazy { builder().create() }
}
