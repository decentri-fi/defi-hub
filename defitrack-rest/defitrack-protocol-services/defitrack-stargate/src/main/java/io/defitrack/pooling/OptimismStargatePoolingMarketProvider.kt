package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.protocol.StargateOptimismService
import org.springframework.stereotype.Component

@Component
class OptimismStargatePoolingMarketProvider(
    stargateService: StargateOptimismService
) : StargatePoolingMarketProvider(
    stargateService
) {
    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}