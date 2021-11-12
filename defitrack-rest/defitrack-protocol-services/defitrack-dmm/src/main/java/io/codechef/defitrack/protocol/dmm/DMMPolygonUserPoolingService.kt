package io.codechef.defitrack.protocol.dmm

import io.codechef.common.network.Network
import io.codechef.defitrack.pool.UserPoolingService
import io.codechef.defitrack.pool.domain.PoolingElement
import io.codechef.protocol.Protocol
import io.codechef.protocol.dmm.DMMPolygonService
import io.codechef.protocol.staking.TokenType
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class DMMPolygonUserPoolingService(
    private val dmmPolygonService: DMMPolygonService,
) : UserPoolingService {

    @Cacheable(cacheNames = ["dmm-lps"], key = "'polygon-' + #address")
    override fun userPoolings(address: String): List<PoolingElement> {
        return dmmPolygonService.getUserPoolings(address).flatMap {
            it.liquidityPositions
        }.map {
            PoolingElement(
                it.pool.id,
                it.liquidityTokenBalance,
                it.pool.token0.symbol + " / " + it.pool.token1.symbol + " LP",
                it.pool.token0.symbol + "-" + it.pool.token1.symbol,
                getNetwork(),
                getProtocol(),
                tokenType = TokenType.DMM
            )
        }
    }

    override fun getProtocol(): Protocol {
        return Protocol.DMM
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}