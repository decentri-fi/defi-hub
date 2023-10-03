package io.defitrack.protocol.stargate.farming

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.StargateBaseService
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STARGATE)
class StargateBaseFarmingProvider(
    stargateService: StargateBaseService,
) : AbstractStargateFarmingMarketProvider(
    stargateService,
    "pendingEmissionToken", "eToken"
) {

    override fun getNetwork(): Network {
        return Network.BASE
    }
}