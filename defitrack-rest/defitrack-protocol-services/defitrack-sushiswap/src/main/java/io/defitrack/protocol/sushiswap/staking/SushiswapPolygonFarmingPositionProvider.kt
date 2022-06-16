package io.defitrack.protocol.sushiswap.staking

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.DefaultFarmingPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class SushiswapPolygonFarmingPositionProvider(
    erC20Resource: ERC20Resource,
    stakingMarketService: SushiswapPolygonFarmingMinichefMarketService,
    blockchainGatewayProvider: BlockchainGatewayProvider
) : DefaultFarmingPositionProvider(erC20Resource, stakingMarketService, blockchainGatewayProvider)