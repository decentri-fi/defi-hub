package io.defitrack.protocol.hop.pooling

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.hop.HopService
import io.defitrack.protocol.hop.apr.HopAPRService
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.HOP)
class HopArbitrumPoolingMarketProvider(
    hopService: HopService,
    hopAPRService: HopAPRService,
) : HopPoolingMarketProvider(hopService, hopAPRService) {

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}