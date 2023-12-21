package io.defitrack.protocol.beefy.staking

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BEEFY)
@ConditionalOnProperty(value = ["polygon.enabled"], havingValue = "true", matchIfMissing = true)
class BeefyPolygonBoostMarketProvider(
    beefyPolygonFarmingMarketProvider: BeefyPolygonFarmingMarketProvider
) : BeefyBoostMarketProvider(beefyPolygonFarmingMarketProvider) {

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}