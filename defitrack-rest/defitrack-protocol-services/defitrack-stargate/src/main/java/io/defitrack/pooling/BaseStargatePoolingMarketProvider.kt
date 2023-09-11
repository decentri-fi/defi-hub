package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.protocol.StargateBaseService
import io.defitrack.protocol.StargateOptimismService
import org.springframework.stereotype.Component

@Component
class BaseStargatePoolingMarketProvider(
    stargateService: StargateBaseService
) : StargatePoolingMarketProvider(
    stargateService
) {
    override fun getNetwork(): Network {
        return Network.BASE
    }
}