package io.defitrack.protocol.sushiswap.pooling

import arrow.fx.coroutines.parMap
import io.defitrack.common.utils.Refreshable
import io.defitrack.common.utils.Refreshable.Companion.refreshable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.sushiswap.SushiswapService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.bytebuddy.pool.TypePool.Empty
import kotlin.coroutines.EmptyCoroutineContext

abstract class DefaultSushiPoolingMarketProvider(
    private val sushiServices: List<SushiswapService>,
) : PoolingMarketProvider() {
    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {
        sushiServices.filter { sushiswapService ->
            sushiswapService.getNetwork() == getNetwork()
        }.flatMap { service ->
            service.getPairs()
                .parMap(EmptyCoroutineContext, 12) {
                    try {
                        throttled {
                            val token = getToken(it.id)
                            val token0 = getToken(it.token0.id)
                            val token1 = getToken(it.token1.id)

                            create(
                                address = it.id,
                                name = token.name,
                                symbol = token.symbol,
                                tokens = listOf(
                                    token0.toFungibleToken(),
                                    token1.toFungibleToken(),
                                ),
                                identifier = it.id,
                                marketSize = refreshable(it.reserveUSD),
                                positionFetcher = defaultPositionFetcher(token.address),
                                totalSupply = refreshable(token.totalDecimalSupply()) {
                                    getToken(it.id).totalDecimalSupply()
                                }
                            )
                        }
                    } catch (ex: Exception) {
                        logger.error("Error while fetching market ${it.id}", ex)
                        null
                    }
                }
        }.filterNotNull()
    }
}