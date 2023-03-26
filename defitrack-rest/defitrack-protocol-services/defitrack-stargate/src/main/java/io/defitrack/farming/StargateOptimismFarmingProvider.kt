package io.defitrack.farming

import io.defitrack.common.network.Network
import io.defitrack.protocol.StargateOptimismService
import org.springframework.stereotype.Component

@Component
class StargateOptimismFarmingProvider(
    stargateOptimismService: StargateOptimismService,
) : StargateFarmingMarketProvider(
    stargateOptimismService,
) {
    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}