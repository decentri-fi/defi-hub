package io.defitrack.protocol.balancer.staking

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.staking.DefaultStakingPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class BalancerPolygonStakingPositionService(
    balancerPolygonStakingMarketService: BalancerPolygonStakingMarketService,
    gateway: BlockchainGatewayProvider,
    erC20Resource: ERC20Resource,
) : DefaultStakingPositionService(erC20Resource, balancerPolygonStakingMarketService, gateway)