package io.defitrack.protocol.stargate.pooling

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.stargate.StargateBaseService
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STARGATE)
class BaseStargatePoolingMarketProvider(
    stargateService: StargateBaseService
) : AbstractStargatePoolingMarketProvider(
    stargateService
) {
    override fun getNetwork(): Network {
        return Network.BASE
    }
}