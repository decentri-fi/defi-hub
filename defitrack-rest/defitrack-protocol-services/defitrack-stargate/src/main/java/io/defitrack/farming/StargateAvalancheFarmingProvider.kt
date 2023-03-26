package io.defitrack.farming

import io.defitrack.common.network.Network
import io.defitrack.protocol.StargateAvalancheService
import org.springframework.stereotype.Component

@Component
class StargateAvalancheFarmingProvider(
    stargateService: StargateAvalancheService,
) : StargateFarmingMarketProvider(
    stargateService
) {
    override fun getNetwork(): Network {
        return Network.AVALANCHE
    }
}