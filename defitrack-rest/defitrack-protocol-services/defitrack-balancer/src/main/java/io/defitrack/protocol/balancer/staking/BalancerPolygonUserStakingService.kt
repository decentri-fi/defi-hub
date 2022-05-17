package io.defitrack.protocol.balancer.staking

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.staking.DefaultUserStakingService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class BalancerPolygonUserStakingService(
    balancerPolygonStakingMarketService: BalancerPolygonStakingMarketService,
    gateway: ContractAccessorGateway,
    erC20Resource: ERC20Resource,
) : DefaultUserStakingService(erC20Resource, balancerPolygonStakingMarketService, gateway) {


    override fun getProtocol(): Protocol {
        return Protocol.BALANCER
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}