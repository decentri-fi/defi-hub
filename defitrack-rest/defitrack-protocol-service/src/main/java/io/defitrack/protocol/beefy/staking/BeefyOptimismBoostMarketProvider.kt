package io.defitrack.protocol.beefy.staking

import io.defitrack.common.network.Network
import io.defitrack.protocol.beefy.BeefyBoostService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["optimism.enabled"], havingValue = "true", matchIfMissing = true)
class BeefyOptimismBoostMarketProvider(
    beefyBoostService: BeefyBoostService
) : BeefyBoostMarketProvider(beefyBoostService.beefyOptimismVaults) {

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}