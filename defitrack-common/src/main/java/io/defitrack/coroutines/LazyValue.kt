package io.defitrack.coroutines

import io.github.reactivecircus.cache4k.Cache

class LazyValue<T : Any>(
    private val value: suspend () -> T
) {

    val cache = Cache.Builder<String, T>().build()

    suspend fun get(): T {
        return cache.get("value") {
            value()
        }
    }
}