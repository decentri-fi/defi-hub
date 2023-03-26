package io.defitrack.protocol.balancer.pooling

import io.defitrack.market.pooling.PoolingMarketProvider
import io.defitrack.market.pooling.domain.PoolingMarket
import io.defitrack.protocol.balancer.BalancerPoolGraphProvider
import io.defitrack.token.TokenType
import java.math.BigDecimal

abstract class BalancerPoolingMarketProvider(
    private val balancerPoolGraphProvider: BalancerPoolGraphProvider
) : PoolingMarketProvider() {

    override suspend fun fetchMarkets(): List<PoolingMarket> {
        return balancerPoolGraphProvider.getPools().mapNotNull {
            if (it.totalLiquidity > BigDecimal.valueOf(100000)) {
                create(
                    identifier = it.id,
                    address = it.address,
                    name = "${
                        it.tokens.joinToString("/") {
                            it.symbol
                        }
                    } Pool",
                    tokens = emptyList(),
                    symbol = it.symbol,
                    apr = BigDecimal.ZERO,
                    marketSize = it.totalLiquidity,
                    tokenType = TokenType.BALANCER,
                    positionFetcher = defaultPositionFetcher(it.address),
                    totalSupply = it.totalShares.toBigInteger()
                )
            } else {
                null
            }
        }
    }
}