package io.defitrack.protocol.stargate.pooling

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.stargate.StargateEthereumService
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STARGATE)
class EthereumStargatePoolingMarketProvider(
    stargateService: StargateEthereumService
) : AbstractStargatePoolingMarketProvider(
    stargateService
) {
    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}