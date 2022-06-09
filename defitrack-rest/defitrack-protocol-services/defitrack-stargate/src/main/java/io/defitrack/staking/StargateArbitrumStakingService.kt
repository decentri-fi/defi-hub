package io.defitrack.staking

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.StargateArbitrumService
import io.defitrack.protocol.StargatePolygonService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class StargateArbitrumStakingService(
    stargateService: StargateArbitrumService,
    accessorGateway: ContractAccessorGateway,
    abiResource: ABIResource,
    erC20Resource: ERC20Resource
) : StargateStakingMarketService(
    stargateService, accessorGateway, abiResource, erC20Resource
) {
    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}