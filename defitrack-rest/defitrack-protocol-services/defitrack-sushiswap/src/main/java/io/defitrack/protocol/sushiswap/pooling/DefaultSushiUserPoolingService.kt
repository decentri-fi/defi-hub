package io.defitrack.protocol.sushiswap.pooling

import io.defitrack.pool.UserPoolingService
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.SushiswapService
import io.defitrack.token.TokenType
import java.math.BigDecimal

abstract class DefaultSushiUserPoolingService(
    private val sushiServices: List<SushiswapService>,
) : UserPoolingService() {

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