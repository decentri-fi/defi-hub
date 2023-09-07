package io.defitrack.common.utils

import kotlinx.coroutines.*

object AsyncUtils {

    suspend fun <T> Deferred<T>.await(timeout: Long, defaultValue: T) =
        withTimeoutOrNull(timeout) { await() } ?: defaultValue

    fun <T> lazyAsync(block: suspend kotlinx.coroutines.CoroutineScope.() -> T): Deferred<T> {
        return GlobalScope.async(Dispatchers.Unconfined, start = CoroutineStart.LAZY) {
            block()
        }
    }
}