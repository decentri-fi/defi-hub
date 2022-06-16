package io.defitrack.protocol.sushiswap.pooling

import io.defitrack.market.pooling.PoolingPositionProvider
import io.defitrack.market.pooling.domain.PoolingElement
import io.defitrack.market.pooling.domain.PoolingMarketElement
import io.defitrack.protocol.SushiswapService
import io.defitrack.token.TokenType
import java.math.BigDecimal

abstract class DefaultSushiPoolingPositionProvider(
    private val sushiServices: List<SushiswapService>,
) : PoolingPositionProvider() {

    override suspend fun fetchUserPoolings(address: String): List<PoolingElement> {
        return sushiServices.filter {
            it.getNetwork() == getNetwork()
        }.flatMap { service ->
            service.getUserPoolings(address).flatMap {
                it.liquidityPositions
            }.filter {
                it.liquidityTokenBalance > BigDecimal.ZERO
            }.map {

                val market = PoolingMarketElement(
                    id = "sushiswap-${getNetwork().slug}-${it.pair.id}",
                    network = getNetwork(),
                    protocol = getProtocol(),
                    address = it.pair.id,
                    name = it.pair.token0.symbol + " / " + it.pair.token1.symbol + " LP",
                    symbol = it.pair.token0.symbol + "-" + it.pair.token1.symbol,
                    tokens = emptyList(),
                    apr = null,
                    marketSize = null,
                    tokenType = TokenType.SUSHISWAP
                )
                poolingElement(
                    market,
                    it.liquidityTokenBalance
                )
            }
        }
    }
}