package io.defitrack.protocol.hop.farming

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.HopService
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.HOP)
class HopPolygonFarmingMarketProvider(
    hopService: HopService,
) : HopFarmingMarketProvider(hopService) {


    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}