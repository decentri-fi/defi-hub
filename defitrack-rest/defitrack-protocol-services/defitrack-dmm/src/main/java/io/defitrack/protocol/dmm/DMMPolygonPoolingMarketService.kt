package io.defitrack.protocol.dmm

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import io.defitrack.pool.domain.PoolingToken
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.dmm.apr.DMMAPRService
import org.springframework.stereotype.Service

@Service
class DMMPolygonPoolingMarketService(
    private val dmmPolygonService: DMMPolygonService,
    private val dmmaprService: DMMAPRService
) : PoolingMarketService() {

    override fun fetchPoolingMarkets(): List<PoolingMarketElement> {
        return dmmPolygonService.getPoolingMarkets().map {
            PoolingMarketElement(
                id = "dmm-polygon-${it.id}",
                network = getNetwork(),
                protocol = getProtocol(),
                address = it.id,
                name = "DMM ${it.token0.symbol}-${it.token1.symbol}",
                token = listOf(
                    PoolingToken(
                        name = it.token0.name,
                        symbol = it.token0.symbol,
                        address = it.token0.id
                    ),
                    PoolingToken(
                        name = it.token1.name,
                        symbol = it.token1.symbol,
                        address = it.token1.id
                    )
                ),
                apr = dmmaprService.getAPR(it.pair.id, getNetwork()),
                marketSize = it.reserveUSD
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