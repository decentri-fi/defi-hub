package io.defitrack.protocol.beefy.staking

import io.defitrack.common.network.Network
import io.defitrack.protocol.beefy.BeefyBoostService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["polygon.enabled"], havingValue = "true", matchIfMissing = true)
class BeefyPolygonBoostMarketProvider(
    beefyBoostService: BeefyBoostService
) : BeefyBoostMarketProvider(beefyBoostService.beefyPolygonVaults) {

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}