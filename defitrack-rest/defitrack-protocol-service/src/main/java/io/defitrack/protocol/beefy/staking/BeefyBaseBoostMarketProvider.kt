package io.defitrack.protocol.beefy.staking

import io.defitrack.common.network.Network
import io.defitrack.protocol.beefy.BeefyBoostService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["base.enabled"], havingValue = "true", matchIfMissing = true)
class BeefyBaseBoostMarketProvider(
    beefyBoostService: BeefyBoostService
) : BeefyBoostMarketProvider(beefyBoostService.beefyBaseVaults) {

    override fun getNetwork(): Network {
        return Network.BASE
    }
}