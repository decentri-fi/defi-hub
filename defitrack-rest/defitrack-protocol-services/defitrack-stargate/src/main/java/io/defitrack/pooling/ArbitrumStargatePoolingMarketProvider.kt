package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.protocol.StargateArbitrumService
import org.springframework.stereotype.Component

@Component
class ArbitrumStargatePoolingMarketProvider(
    stargateArbitrumService: StargateArbitrumService
) : StargatePoolingMarketProvider(
    stargateArbitrumService
) {
    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}