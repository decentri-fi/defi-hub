package io.defitrack.protocol.balancer.staking

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.DefaultFarmingPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class BalancerPolygonFarmingPositionProvider(
    balancerPolygonStakingMarketService: BalancerPolygonFarmingMarketService,
    gateway: BlockchainGatewayProvider,
    erC20Resource: ERC20Resource,
) : DefaultFarmingPositionProvider(erC20Resource, balancerPolygonStakingMarketService, gateway)