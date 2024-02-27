package io.defitrack.protocol.application.sushiswap.staking

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.sushiswap.SushiArbitrumService
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SUSHISWAP)
class SushiswapArbitrumFarmingMinichefMarketProvider : SushiMinichefV2FarmingMarketProvider(
    SushiArbitrumService.getMiniChefs()
) {

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}