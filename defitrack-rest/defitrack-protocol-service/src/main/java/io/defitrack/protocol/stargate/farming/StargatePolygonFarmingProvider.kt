package io.defitrack.protocol.stargate.farming

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.stargate.StargatePolygonService
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STARGATE)
class StargatePolygonFarmingProvider(
    stargateService: StargatePolygonService,
) : AbstractStargateFarmingMarketProvider(
    stargateService
) {
    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}