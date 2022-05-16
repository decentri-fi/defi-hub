package io.defitrack.protocol.dmm

import io.defitrack.common.network.Network
import io.defitrack.pool.UserPoolingService
import io.defitrack.pool.domain.PoolingElement
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.protocol.Protocol
import io.defitrack.token.TokenType
import org.springframework.stereotype.Service

@Service
class DMMPolygonUserPoolingService(
    private val dmmPolygonService: DMMPolygonService,
) : UserPoolingService() {

    override suspend fun fetchUserPoolings(address: String): List<PoolingElement> {
        return dmmPolygonService.getUserPoolings(address).flatMap {
            it.liquidityPositions
        }.map {

            val market = PoolingMarketElement(
                id = "dmm-polygon-${it.pool.id}",
                network = getNetwork(),
                protocol = getProtocol(),
                address = it.pool.id,
                name = it.pool.token0.symbol + " / " + it.pool.token1.symbol + " LP",
                symbol = it.pool.token0.symbol + "-" + it.pool.token1.symbol,
                tokens = emptyList(),
                apr = null,
                marketSize = null,
                tokenType = TokenType.DMM
            )
            poolingElement(
                market,
                it.liquidityTokenBalance
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