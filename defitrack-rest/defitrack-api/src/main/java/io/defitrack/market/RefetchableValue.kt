package io.defitrack.market

import kotlinx.coroutines.runBlocking

class RefetchableValue<T> {

    companion object {
        fun <T> refetchable(initialValue: T, fetcher: (suspend () -> T)? = null): RefetchableValue<T> {
            return RefetchableValue(initialValue, fetcher)
        }

        fun <T> refetchable(fetcher: (suspend () -> T)): RefetchableValue<T> {
            return RefetchableValue(null, fetcher)
        }
    }

    private var value: T

    val fetcher: (suspend () -> T)?

    private constructor(initialValue: T?, fetcher: (suspend () -> T)? = null) {
        if (initialValue == null && fetcher == null) {
            throw IllegalArgumentException("initialValue and fetcher cannot both be null")
        }

        if (initialValue == null) {
            this.value = runBlocking { fetcher!!() }
        } else {
            this.value = initialValue
        }

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