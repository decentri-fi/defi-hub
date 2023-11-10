package io.defitrack.protocol.beefy.staking

import io.defitrack.BulkConstantResolver
import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.beefy.BeefyVaultService
import io.defitrack.protocol.beefy.apy.BeefyAPYService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BEEFY)
@ConditionalOnProperty(value = ["ethereum.enabled"], havingValue = "true", matchIfMissing = true)
class BeefyEthereumFarmingMarketProvider(
    beefyAPYService: BeefyAPYService,
    beefyService: BeefyVaultService,
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