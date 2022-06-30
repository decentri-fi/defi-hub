package io.defitrack.market.pooling

import io.defitrack.market.pooling.domain.PoolingElement
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.ProtocolService
import io.github.reactivecircus.cache4k.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import kotlin.time.Duration.Companion.minutes

abstract class PoolingPositionProvider : ProtocolService {

    val cache = Cache.Builder().expireAfterWrite(
        1.minutes
    ).build<String, List<PoolingElement>>()

    fun userPoolings(address: String): List<PoolingElement> {
        return runBlocking(Dispatchers.IO) {
            cache.get("${getProtocol().slug}-${getNetwork().slug}-$address") {
                fetchUserPoolings(address)
            }
        }
    }

    fun poolingElement(
        market: PoolingMarket,
        amount: BigDecimal,
    ): PoolingElement {
        return PoolingElement(
            lpAddress = market.address,
            amount = amount,
            name = market.name,
            symbol = market.symbol,
            network = getNetwork(),
            protocol = getProtocol(),
            tokenType = market.tokenType,
            id = market.id,
            market = market
        )
    }

    abstract suspend fun fetchUserPoolings(address: String): List<PoolingElement>
}