package io.defitrack.market.pooling

import io.defitrack.market.pooling.domain.PoolingPosition
import io.github.reactivecircus.cache4k.Cache
import kotlin.time.Duration.Companion.minutes

abstract class PoolingPositionProvider {

    val cache = Cache.Builder().expireAfterWrite(
        1.minutes
    ).build<String, List<PoolingPosition>>()

    suspend fun userPoolings(address: String): List<PoolingPosition> {
        return cache.get(address) {
            fetchUserPoolings(address)
        }
    }

    abstract suspend fun fetchUserPoolings(address: String): List<PoolingPosition>
}