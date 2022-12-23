package io.defitrack.market.pooling

import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.market.pooling.domain.PoolingPosition
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import kotlin.time.Duration.Companion.minutes

abstract class PoolingPositionProvider {

    val cache = Cache.Builder().expireAfterWrite(
        1.minutes
    ).build<String, List<PoolingPosition>>()

    fun userPoolings(address: String): List<PoolingPosition> {
        return runBlocking(Dispatchers.IO) {
            cache.get(address) {
                fetchUserPoolings(address)
            }
        }
    }
    abstract suspend fun fetchUserPoolings(address: String): List<PoolingPosition>
}