package io.defitrack.farming

import io.defitrack.common.network.Network
import io.defitrack.protocol.StargatePolygonService
import org.springframework.stereotype.Component

@Component
class StargatePolygonFarmingProvider(
    stargateService: StargatePolygonService,
) : StargateFarmingMarketProvider(
    stargateService
) {
    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}