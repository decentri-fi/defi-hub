package io.defitrack.protocol.beefy.staking

import io.defitrack.BulkConstantResolver
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.beefy.BeefyService
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BEEFY)
class BeefyEthereumFarmingMarketProvider(
    beefyAPYService: BeefyAPYService,
    beefyService: BeefyService,
    bulkConstantResolver: BulkConstantResolver,
) : BeefyFarmingMarketProvider(
    beefyAPYService,
    beefyService.beefyEthereumVaults,
    bulkConstantResolver
) {

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}