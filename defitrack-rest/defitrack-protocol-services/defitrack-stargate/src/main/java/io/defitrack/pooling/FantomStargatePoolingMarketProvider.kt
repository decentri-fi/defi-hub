package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.protocol.StargateFantomService
import io.defitrack.protocol.StargateOptimismService
import io.defitrack.protocol.StargatePolygonService
import org.springframework.stereotype.Component

@Component
class FantomStargatePoolingMarketProvider(
    stargateService: StargateFantomService
) : StargatePoolingMarketProvider(
    stargateService
) {
    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}