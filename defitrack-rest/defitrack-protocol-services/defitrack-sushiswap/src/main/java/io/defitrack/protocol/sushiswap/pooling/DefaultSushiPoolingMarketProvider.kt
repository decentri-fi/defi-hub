package io.defitrack.protocol.sushiswap.pooling

import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.SushiswapService
import io.defitrack.protocol.sushiswap.apr.SushiPoolingAPRCalculator
import io.defitrack.token.TokenType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

abstract class DefaultSushiPoolingMarketProvider(
    private val sushiServices: List<SushiswapService>,
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> = coroutineScope {

        val semaphore = Semaphore(16)

        sushiServices.filter { sushiswapService ->
            sushiswapService.getNetwork() == getNetwork()
        }.flatMap { service ->
            service.getPairs()
                .map {
                    async {
                        try {
                            semaphore.withPermit {
                                val token = getToken(it.id)
                                val token0 = getToken(it.token0.id)
                                val token1 = getToken(it.token1.id)

                                PoolingMarket(
                                    network = service.getNetwork(),
                                    protocol = getProtocol(),
                                    address = it.id,
                                    name = token.name,
                                    symbol = token.symbol,
                                    tokens = listOf(
                                        token0.toFungibleToken(),
                                        token1.toFungibleToken(),
                                    ),
                                    apr = SushiPoolingAPRCalculator(service, it.id).calculateApr(),
                                    id = "sushi-${getNetwork().slug}-${it.id}",
                                    marketSize = it.reserveUSD,
                                    tokenType = TokenType.SUSHISWAP,
                                    positionFetcher = defaultPositionFetcher(token.address),
                                    totalSupply = token.totalSupply
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