package com.saurabh.mediadminapp.data.v1.core

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdempotencyKeyProvider @Inject constructor() {
    fun newKey(): String = UUID.randomUUID().toString()
}
