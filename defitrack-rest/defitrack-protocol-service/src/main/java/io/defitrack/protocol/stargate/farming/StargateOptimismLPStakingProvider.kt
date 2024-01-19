package io.defitrack.protocol.stargate.farming

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.stargate.StargateOptimismService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STARGATE)
@ConditionalOnProperty(value = ["optimism.enabled"], havingValue = "true", matchIfMissing = true)
class StargateOptimismLPStakingProvider(
    stargateOptimismService: StargateOptimismService,
) : AbstractStargateLPStakingMarketProvider(
    stargateOptimismService,
) {
    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}