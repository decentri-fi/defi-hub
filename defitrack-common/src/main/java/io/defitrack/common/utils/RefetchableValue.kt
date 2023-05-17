package io.defitrack.common.utils

class RefetchableValue<T> {

    companion object {
        fun <T> refetchable(initialValue: T, fetcher: (suspend () -> T)? = null): RefetchableValue<T> {
            return RefetchableValue(initialValue, fetcher)
        }

        suspend fun <T> refetchable(fetcher: (suspend () -> T)): RefetchableValue<T> {
            val initial = fetcher()
            return RefetchableValue(initial, fetcher)
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