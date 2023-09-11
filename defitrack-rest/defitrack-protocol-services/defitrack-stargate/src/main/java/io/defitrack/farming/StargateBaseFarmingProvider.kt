package io.defitrack.farming

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.StargateArbitrumService
import io.defitrack.protocol.StargateBaseService
import org.springframework.stereotype.Component

@Component
class StargateBaseFarmingProvider(
    stargateService: StargateBaseService,
) : StargateFarmingMarketProvider(
    stargateService,
    "pendingEmissionToken", "eToken"
) {

    override fun getNetwork(): Network {
        return Network.BASE
    }
}