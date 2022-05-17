package io.defitrack.protocol.balancer.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultUserStakingService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class BalancerPolygonUserStakingService(
    balancerPolygonStakingMarketService: BalancerPolygonStakingMarketService,
    gateway: ContractAccessorGateway,
    erC20Resource: ERC20Resource,
) : DefaultUserStakingService(erC20Resource, balancerPolygonStakingMarketService, gateway)