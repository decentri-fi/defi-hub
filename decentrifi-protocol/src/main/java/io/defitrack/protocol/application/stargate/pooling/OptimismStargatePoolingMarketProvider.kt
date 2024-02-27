package io.defitrack.protocol.application.stargate.pooling

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.stargate.StargateOptimismService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.STARGATE)
@ConditionalOnProperty(value = ["optimism.enabled"], havingValue = "true", matchIfMissing = true)
class OptimismStargatePoolingMarketProvider(
    stargateService: StargateOptimismService
) : AbstractStargatePoolingMarketProvider(
    stargateService
) {
    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}