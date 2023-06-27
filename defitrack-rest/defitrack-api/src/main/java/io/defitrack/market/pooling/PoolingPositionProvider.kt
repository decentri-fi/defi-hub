package io.defitrack.market.pooling

import io.defitrack.market.pooling.domain.PoolingPosition
import io.github.reactivecircus.cache4k.Cache
import kotlin.time.Duration.Companion.minutes

abstract class PoolingPositionProvider {

    val cache = Cache.Builder<String, List<PoolingPosition>>().expireAfterWrite(
        1.minutes
    ).build()

    suspend fun userPoolings(protocol: String, address: String): List<PoolingPosition> {
        return cache.get("$protocol-$address") {
            fetchUserPoolings(protocol, address)
        }
    }

    abstract suspend fun fetchUserPoolings(protocol: String, address: String): List<PoolingPosition>
}