package io.defitrack.farming

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.StargateAvalancheService
import org.springframework.stereotype.Component

@Component
class StargateAvalancheFarmingProvider(
    stargateService: StargateAvalancheService,
    accessorGateway: BlockchainGatewayProvider,
    abiResource: ABIResource,
) : StargateFarmingMarketProvider(
    stargateService, accessorGateway, abiResource
) {
    override fun getNetwork(): Network {
        return Network.AVALANCHE
    }
}