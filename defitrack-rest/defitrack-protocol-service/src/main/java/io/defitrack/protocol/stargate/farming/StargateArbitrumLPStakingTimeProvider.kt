package io.defitrack.protocol.stargate.farming

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.stargate.StargateArbitrumService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STARGATE)
@ConditionalOnProperty(value = ["arbitrum.enabled"], havingValue = "true", matchIfMissing = true)
class StargateArbitrumLPStakingTimeProvider(
    stargateService: StargateArbitrumService,
) : AbstractStargateLPStakingTimeMarketProvider(
    stargateService
) {

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}