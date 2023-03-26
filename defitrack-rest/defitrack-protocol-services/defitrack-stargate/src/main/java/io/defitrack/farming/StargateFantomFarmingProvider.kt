package io.defitrack.farming

import io.defitrack.common.network.Network
import io.defitrack.protocol.StargateFantomService
import org.springframework.stereotype.Component

@Component
class StargateFantomFarmingProvider(
    stargateService: StargateFantomService,
) : StargateFarmingMarketProvider(
    stargateService,
) {
    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}