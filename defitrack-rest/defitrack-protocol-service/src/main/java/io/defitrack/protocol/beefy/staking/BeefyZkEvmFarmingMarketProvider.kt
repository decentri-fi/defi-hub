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
@ConditionalOnProperty(value = ["polygon-zkevm.enabled"], havingValue = "true", matchIfMissing = true)
class BeefyZkEvmFarmingMarketProvider(
    beefyAPYService: BeefyAPYService,
    beefyService: BeefyVaultService,
) : BeefyFarmingMarketProvider(
    beefyAPYService,
    beefyService.beefyPolygonZkEvmVaults,
) {

    override fun getNetwork(): Network {
        return Network.POLYGON_ZKEVM
    }
}