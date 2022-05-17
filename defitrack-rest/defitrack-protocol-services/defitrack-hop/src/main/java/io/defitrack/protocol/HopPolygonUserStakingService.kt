package io.defitrack.protocol

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultUserStakingService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class HopPolygonUserStakingService(
    hopPolygonStakingMarketService: HopPolygonStakingMarketService,
    gateway: ContractAccessorGateway,
    erC20Resource: ERC20Resource,
) : DefaultUserStakingService(erC20Resource, hopPolygonStakingMarketService, gateway) {
    override fun getProtocol(): Protocol {
        return Protocol.HOP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}