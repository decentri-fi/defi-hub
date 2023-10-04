package io.defitrack.protocol.stargate.pooling

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.stargate.StargateArbitrumService
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STARGATE)
class ArbitrumStargatePoolingMarketProvider(
    stargateArbitrumService: StargateArbitrumService
) : AbstractStargatePoolingMarketProvider(
    stargateArbitrumService
) {
    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}