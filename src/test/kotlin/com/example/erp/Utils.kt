package com.example.erp

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

suspend fun waitUntil(
    interval: Duration = 100.milliseconds,
    timeout: Duration = 5.seconds,
    condition: () -> Boolean
) {
    val startTime = System.currentTimeMillis()
    var elapsedTimeMillis = 0L
    while (elapsedTimeMillis < timeout.inWholeMilliseconds) {
        if (condition()) {
            return
        }
        elapsedTimeMillis = System.currentTimeMillis() - startTime
        delay(interval.inWholeMilliseconds)
    }
    false shouldBe true
}