package io.defitrack.common.utils

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.withTimeoutOrNull

object AsyncUtils {

    suspend fun <T> Deferred<T>.await(timeout: Long, defaultValue: T) =
        withTimeoutOrNull(timeout) { await() } ?: defaultValue
}