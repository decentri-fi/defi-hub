package io.defitrack.evm.web3j

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.max

class SimpleRateLimiter(eventsPerSecond: Double) {

    private val mutex = Mutex()

    @Volatile
    private var next: Long = Long.MIN_VALUE
    private val delayNanos: Long = (1_000_000_000L / eventsPerSecond).toLong()

    /**
     * Suspend the current coroutine until it's calculated time of exit
     * from the rate limiter
     */
    suspend fun acquire() {
        val now: Long = System.nanoTime()
        val until = mutex.withLock {
            max(next, now).also {
                next = it + delayNanos
            }
        }
        if (until != now) {
            delay((until - now) / 1_000_000)
        }
    }
}