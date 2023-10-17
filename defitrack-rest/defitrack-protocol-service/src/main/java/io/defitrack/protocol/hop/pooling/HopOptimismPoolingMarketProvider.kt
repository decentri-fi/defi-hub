package io.defitrack.protocol.hop.pooling

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.hop.HopService
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.HOP)
class HopOptimismPoolingMarketProvider(
    hopService: HopService,
) : HopPoolingMarketProvider(hopService) {
    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}