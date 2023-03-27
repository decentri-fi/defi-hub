package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.protocol.StargateEthereumService
import io.defitrack.protocol.StargateOptimismService
import org.springframework.stereotype.Component

@Component
class EthereumStargatePoolingMarketProvider(
    stargateService: StargateEthereumService
) : StargatePoolingMarketProvider(
    stargateService
) {
    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}