package io.defitrack.protocol.stargate.farming

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.stargate.StargateBaseService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STARGATE)
@ConditionalOnProperty(value = ["base.enabled"], havingValue = "true", matchIfMissing = true)
class StargateBaseLPStakingProvider(
    stargateService: StargateBaseService,
) : AbstractStargateLPStakingMarketProvider(
    stargateService,
    "pendingEmissionToken", "eToken"
) {

    override fun getNetwork(): Network {
        return Network.BASE
    }
}