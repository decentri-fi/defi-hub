package io.defitrack.protocol.stargate.farming

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.StargateOptimismService
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STARGATE)
class StargateOptimismFarmingProvider(
    stargateOptimismService: StargateOptimismService,
) : AbstractStargateFarmingMarketProvider(
    stargateOptimismService,
) {
    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}