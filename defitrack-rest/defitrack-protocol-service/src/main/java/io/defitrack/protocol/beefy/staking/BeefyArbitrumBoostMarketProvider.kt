package io.defitrack.protocol.beefy.staking

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.beefy.BeefyBoostService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BEEFY)
@ConditionalOnProperty(value = ["arbitrum.enabled"], havingValue = "true", matchIfMissing = true)
class BeefyArbitrumBoostMarketProvider(
    beefyBoostService: BeefyBoostService,
    beefyArbitrumFarmingMarketProvider: BeefyArbitrumFarmingMarketProvider
) : BeefyBoostMarketProvider(
    beefyBoostService.beefyArbitrumVaults,
    beefyArbitrumFarmingMarketProvider
) {

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}