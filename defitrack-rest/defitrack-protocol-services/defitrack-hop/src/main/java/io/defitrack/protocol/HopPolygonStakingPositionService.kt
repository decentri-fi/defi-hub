package io.defitrack.protocol

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultStakingPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class HopPolygonStakingPositionService(
    hopPolygonStakingMarketService: HopPolygonStakingMarketService,
    gateway: ContractAccessorGateway,
    erC20Resource: ERC20Resource,
) : DefaultStakingPositionService(erC20Resource, hopPolygonStakingMarketService, gateway)
