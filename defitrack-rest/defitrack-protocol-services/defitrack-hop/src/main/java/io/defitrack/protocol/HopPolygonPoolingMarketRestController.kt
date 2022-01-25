package io.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.pool.PoolingMarketService
import io.defitrack.pool.domain.PoolingMarketElement
import org.springframework.stereotype.Component

@Component
class HopPolygonPoolingMarketRestController(private val hopService: HopService) : PoolingMarketService() {
    override fun fetchPoolingMarkets(): List<PoolingMarketElement> {
        hopService.polygonLps(getNetwork())
        TODO("Not yet implemented")
    }

    override fun getProtocol(): Protocol {
        return Protocol.HOP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}