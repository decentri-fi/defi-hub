package io.defitrack.protocol.pooling

import io.defitrack.common.network.Network
import io.defitrack.protocol.HopService
import io.defitrack.protocol.apr.HopAPRService
import org.springframework.stereotype.Component

@Component
class HopPolygonPoolingMarketService(
    hopService: HopService,
    hopAPRService: HopAPRService,
) : HopPoolingMarketProvider(
    hopService, hopAPRService
) {

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}