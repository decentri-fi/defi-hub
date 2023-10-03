package io.defitrack.protocol.stargate.pooling

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.StargateOptimismService
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STARGATE)
class OptimismStargatePoolingMarketProvider(
    stargateService: StargateOptimismService
) : AbstractStargatePoolingMarketProvider(
    stargateService
) {
    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}