package io.defitrack.protocol.sushiswap.pooling

import io.defitrack.pool.UserPoolingService
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SushiswapService
import io.defitrack.protocol.staking.TokenType
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SushiswapPolygonUserPoolingService(
    private val sushiServices: List<SushiswapService>,
) : UserPoolingService {

    @Cacheable(cacheNames = ["sushiswap-lps"], key = "'polygon-' + #address")
    override fun userPoolings(address: String): List<PoolingElement> {
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
                    tokenType = TokenType.SUSHISWAP
                )
            }
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}