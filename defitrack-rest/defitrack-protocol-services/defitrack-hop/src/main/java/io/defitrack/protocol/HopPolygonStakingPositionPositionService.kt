package io.defitrack.protocol

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultStakingPositionPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class HopPolygonStakingPositionPositionService(
    hopPolygonStakingMarketService: HopPolygonStakingMarketService,
    gateway: ContractAccessorGateway,
    erC20Resource: ERC20Resource,
) : DefaultStakingPositionPositionService(erC20Resource, hopPolygonStakingMarketService, gateway)
