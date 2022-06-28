package io.defitrack.protocol

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.DefaultFarmingPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class HopPolygonFarmingPositionProvider(
    hopPolygonStakingMarketService: HopPolygonFarmingMarketProvider,
    gateway: BlockchainGatewayProvider,
    erC20Resource: ERC20Resource,
) : DefaultFarmingPositionProvider(erC20Resource, hopPolygonStakingMarketService, gateway)
