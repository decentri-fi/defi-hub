package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.protocol.StargateAvalancheService
import org.springframework.stereotype.Component

@Component
class AvalancheStargatePoolingMarketProvider(
    stargateArbitrumService: StargateAvalancheService
) : StargatePoolingMarketProvider(
    stargateArbitrumService
) {
    override fun getNetwork(): Network {
        return Network.AVALANCHE
    }
}