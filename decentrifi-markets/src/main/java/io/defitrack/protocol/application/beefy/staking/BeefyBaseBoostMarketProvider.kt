package io.defitrack.protocol.application.beefy.staking

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.beefy.BeefyBoostService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BEEFY)
@ConditionalOnProperty(value = ["base.enabled"], havingValue = "true", matchIfMissing = true)
class BeefyBaseBoostMarketProvider(
    beefyBaseFarmingMarketProvider: BeefyBaseFarmingMarketProvider
) : BeefyBoostMarketProvider(beefyBaseFarmingMarketProvider) {

    override fun getNetwork(): Network {
        return Network.BASE
    }
}