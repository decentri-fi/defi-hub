package io.defitrack.farming

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.StargateAvalancheService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class StargateAvalancheFarmingProvider(
    stargateService: StargateAvalancheService,
    accessorGateway: BlockchainGatewayProvider,
    abiResource: ABIResource,
    erC20Resource: ERC20Resource
) : StargateFarmingMarketProvider(
    stargateService, accessorGateway, abiResource, erC20Resource
) {
    override fun getNetwork(): Network {
        return Network.AVALANCHE
    }
}