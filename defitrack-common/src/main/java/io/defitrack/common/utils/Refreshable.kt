package io.defitrack.common.utils

class Refreshable<T> {

    companion object {
        fun <T> refreshable(initialValue: T, fetcher: (suspend () -> T)? = null): Refreshable<T> {
            return Refreshable(initialValue, fetcher)
        }

        suspend fun <T> refreshable(fetcher: (suspend () -> T)): Refreshable<T> {
            val initial = fetcher()
            return Refreshable(initial, fetcher)
        }
    }

    private var value: T

    val fetcher: (suspend () -> T)?

    private constructor(initialValue: T, fetcher: (suspend () -> T)? = null) {
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
        }
    }
}