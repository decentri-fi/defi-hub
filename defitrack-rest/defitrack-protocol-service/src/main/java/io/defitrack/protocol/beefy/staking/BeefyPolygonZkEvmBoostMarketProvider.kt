package io.defitrack.protocol.beefy.staking

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.beefy.BeefyBoostService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BEEFY)
@ConditionalOnProperty(value = ["polygon-zkevm.enabled"], havingValue = "true", matchIfMissing = true)
class BeefyPolygonZkEvmBoostMarketProvider(
    beefyBoostService: BeefyBoostService,
    beefyZkEvmFarmingMarketProvider: BeefyZkEvmFarmingMarketProvider
) : BeefyBoostMarketProvider(beefyBoostService.beefyPolygonZkEvmVaults, beefyZkEvmFarmingMarketProvider) {

    override fun getNetwork(): Network {
        return Network.POLYGON_ZKEVM
    }
}