package io.defitrack.farming

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.StargateOptimismService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class StargateOptimismFarmingProvider(
    stargateOptimismService: StargateOptimismService,
    accessorGateway: BlockchainGatewayProvider,
    abiResource: ABIResource,
) : StargateFarmingMarketProvider(
    stargateOptimismService, accessorGateway, abiResource
) {
    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}