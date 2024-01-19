package io.defitrack.protocol.beefy.staking

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.beefy.BeefyVaultService
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BEEFY)
@ConditionalOnProperty(value = ["optimism.enabled"], havingValue = "true", matchIfMissing = true)
class BeefyOptimismFarmingMarketProvider : BeefyFarmingMarketProvider() {

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}