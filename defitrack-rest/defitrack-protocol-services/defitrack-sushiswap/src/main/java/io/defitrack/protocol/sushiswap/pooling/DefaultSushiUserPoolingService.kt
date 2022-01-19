package io.defitrack.protocol.sushiswap.pooling

import io.defitrack.pool.UserPoolingService
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.protocol.SushiswapService
import io.defitrack.protocol.staking.TokenType
import java.math.BigDecimal

abstract class DefaultSushiUserPoolingService(
    private val sushiServices: List<SushiswapService>,
) : UserPoolingService() {

    override fun fetchUserPoolings(address: String): List<PoolingElement> {
        return sushiServices.filter {
            it.getNetwork() == getNetwork()
        }.flatMap { service ->
            service.getUserPoolings(address).flatMap {
                it.liquidityPositions
            }.filter {
                it.liquidityTokenBalance > BigDecimal.ZERO
            }.map {
                PoolingElement(
                    it.pair.id,
                    it.liquidityTokenBalance,
                    it.pair.token0.symbol + " / " + it.pair.token1.symbol + " LP",
                    it.pair.token0.symbol + "-" + it.pair.token1.symbol,
                    service.getNetwork(),
                    getProtocol(),
                    tokenType = TokenType.SUSHISWAP,
                    "sushiswap-${getNetwork().slug}-${it.pair.id}",
                )
            }
        }
    }
}