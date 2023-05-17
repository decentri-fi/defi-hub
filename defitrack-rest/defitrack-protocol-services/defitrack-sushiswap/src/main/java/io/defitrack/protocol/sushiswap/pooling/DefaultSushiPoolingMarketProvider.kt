package io.defitrack.protocol.sushiswap.pooling

import io.defitrack.common.utils.RefetchableValue
import io.defitrack.common.utils.RefetchableValue.Companion.refetchable
import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SushiswapService
import io.defitrack.protocol.sushiswap.apr.SushiPoolingAPRCalculator
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

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
                .map {
                    async {
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
                                    apr = SushiPoolingAPRCalculator(service, it.id).calculateApr(),
                                    identifier = it.id,
                                    marketSize = refetchable(it.reserveUSD),
                                    tokenType = TokenType.SUSHISWAP,
                                    positionFetcher = defaultPositionFetcher(token.address),
                                    totalSupply = RefetchableValue.refetchable(token.totalDecimalSupply()) {
                                        getToken(it.id).totalDecimalSupply()
                                    }
                                )
                            }
                        } catch (ex: Exception) {
                            logger.error("Error while fetching market ${it.id}", ex)
                            null
                        }
                    }
                }
        }.awaitAll().filterNotNull()
    }
}