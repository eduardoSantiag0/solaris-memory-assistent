package dev.solaris.memory.network

import kotlin.random.Random

/** Good enough for an idempotency key (doc section 8) — doesn't need to be a real UUID. */
fun randomRequestId(): String {
    val chars = "0123456789abcdef"
    return buildString {
        repeat(32) { append(chars[Random.nextInt(chars.length)]) }
    }
}
