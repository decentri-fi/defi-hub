package io.defitrack.protocol.farming

import io.defitrack.common.network.Network
import io.defitrack.protocol.HopService
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Component

@Component
class HopPolygonFarmingMarketProvider(
    hopService: HopService,
) : HopFarmingMarketProvider(hopService) {


    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}