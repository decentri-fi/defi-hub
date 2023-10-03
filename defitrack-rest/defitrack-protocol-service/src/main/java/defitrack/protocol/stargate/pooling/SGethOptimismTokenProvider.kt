package io.defitrack.protocol.stargate.pooling

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STARGATE)
class SGethOptimismTokenProvider : AbstractSGethTokenProvider(
    address = "0xb69c8cbcd90a39d8d3d3ccf0a3e968511c3856a0"
) {
    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}