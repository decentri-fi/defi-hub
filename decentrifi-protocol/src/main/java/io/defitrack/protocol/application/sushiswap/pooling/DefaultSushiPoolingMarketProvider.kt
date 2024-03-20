package io.defitrack.protocol.sushiswap.pooling

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.fx.coroutines.parMap
import io.defitrack.common.utils.refreshable
import io.defitrack.common.utils.toRefreshable
import io.defitrack.market.port.out.PoolingMarketProvider
import io.defitrack.market.domain.PoolingMarket
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.sushiswap.SushiswapService
import io.defitrack.protocol.sushiswap.domain.SushiswapPair

abstract class DefaultSushiPoolingMarketProvider(
    private val sushiServices: List<SushiswapService>,
) : PoolingMarketProvider() {
    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override suspend fun fetchMarkets(): List<PoolingMarket> {
        return sushiServices.filter { sushiswapService ->
            sushiswapService.getNetwork() == getNetwork()
        }.flatMap { service ->
            service.getPairs()
                .parMap(concurrency = 12) {
                    createMarket(it)
                }
        }.mapNotNull {
            it.mapLeft {
                logger.error("Failed to create market: {}", it.message)
            }.getOrNull()
        }
    }

    private suspend fun createMarket(it: SushiswapPair): Either<Throwable, PoolingMarket> {
        return catch {
            val token = getToken(it.id)
            val token0 = getToken(it.token0.id)
            val token1 = getToken(it.token1.id)

            val breakdown = breakdownOf(
                token.address,
                token0, token1
            )

            create(
                address = it.id,
                name = token.name,
                symbol = token.symbol,
                tokens = listOf(token0, token1),
                breakdown = refreshable { breakdown },
                identifier = it.id,
                positionFetcher = defaultPositionFetcher(token.address),
                totalSupply = refreshable(token.totalDecimalSupply()) {
                    getToken(it.id).totalDecimalSupply()
                }
            )
        }
    }
}