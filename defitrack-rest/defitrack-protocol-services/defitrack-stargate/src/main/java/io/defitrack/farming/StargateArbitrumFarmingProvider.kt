package io.defitrack.farming

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.StargateArbitrumService
import org.springframework.stereotype.Component

@Component
class StargateArbitrumFarmingProvider(
    stargateService: StargateArbitrumService,
) : StargateFarmingMarketProvider(
    stargateService
) {
    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}