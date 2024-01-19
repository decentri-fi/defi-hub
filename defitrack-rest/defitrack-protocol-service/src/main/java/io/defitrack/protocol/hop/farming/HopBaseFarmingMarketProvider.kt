package io.defitrack.protocol.hop.farming

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.hop.HopService
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.HOP)
class HopBaseFarmingMarketProvider(
    hopService: HopService,
) : HopFarmingMarketProvider(hopService) {

    override fun getNetwork(): Network {
        return Network.BASE
    }
}