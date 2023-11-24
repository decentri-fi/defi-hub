package io.defitrack.protocol.beefy.staking

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.beefy.BeefyVaultService
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BEEFY)
@ConditionalOnProperty(value = ["optimism.enabled"], havingValue = "true", matchIfMissing = true)
class BeefyOptimismFarmingMarketProvider(
    beefyAPYService: BeefyAPYService,
    beefyService: BeefyVaultService,
) : BeefyFarmingMarketProvider(
    beefyAPYService,
    beefyService.beefyOptimismVaults,
) {

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}