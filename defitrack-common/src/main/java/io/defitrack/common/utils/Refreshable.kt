package io.defitrack.common.utils

import org.slf4j.LoggerFactory

class Refreshable<T>(initialValue: T, fetcher: (suspend () -> T)? = null) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private var value: T

    val fetcher: (suspend () -> T)?

    init {
        if (initialValue == null && fetcher == null) {
            throw IllegalArgumentException("initialValue and fetcher cannot both be null")
        }

        this.value = initialValue
        this.fetcher = fetcher
    }


    fun get(): T {
        return value
    }

    suspend fun refresh() {
        fetcher?.let { fetcher ->
            this.value = fetcher()
        }?.also {
            logger.debug("Refreshed {} to {}", this::class.simpleName, value)
        }
    }
}

fun <T> refreshable(initialValue: T, fetcher: (suspend () -> T)? = null): Refreshable<T> {
    return Refreshable(initialValue, fetcher)
}

suspend fun <T> refreshable(fetcher: (suspend () -> T)): Refreshable<T> {
    val initial = fetcher()
    return Refreshable(initial, fetcher)
}

fun <T> T.toRefreshable(): Refreshable<T> {
    return Refreshable(this)
}

suspend fun <T> (suspend () -> T).toRefreshable(): Refreshable<T> {
    return refreshable(this)
}

suspend inline fun <T, R> Refreshable<T>.map(crossinline transform: suspend (T) -> R): Refreshable<R> {
    return refreshable(
        initialValue = transform(get()),
        fetcher = fetcher?.let {
            { transform(it.invoke()) }
        }
    )
}