package io.defitrack.protocol.application.sushiswap.staking

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.SUSHISWAP)
class SushiswapArbitrumFarmingMinichefMarketProvider : SushiMinichefV2FarmingMarketProvider(
    listOf("0xf4d73326c13a4fc5fd7a064217e12780e9bd62c3")
) {

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}