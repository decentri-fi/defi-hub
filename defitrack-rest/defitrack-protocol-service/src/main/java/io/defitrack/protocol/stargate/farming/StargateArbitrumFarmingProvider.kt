package io.defitrack.protocol.stargate.farming

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.StargateArbitrumService
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STARGATE)
class StargateArbitrumFarmingProvider(
    stargateService: StargateArbitrumService,
) : AbstractStargateFarmingMarketProvider(
    stargateService
) {

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}