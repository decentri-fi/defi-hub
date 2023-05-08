package io.defitrack.pooling

import io.defitrack.common.network.Network
import org.springframework.stereotype.Component

@Component
class SGethOptimismTokenProvider : SGethTokenProvider(
    address = "0xb69c8cbcd90a39d8d3d3ccf0a3e968511c3856a0"
) {
    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}