package io.defitrack.protocol.sushiswap.staking

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.DefaultFarmingPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class SushiswapFantomFarmingPositionProvider(
    erC20Resource: ERC20Resource,
    stakingMarketService: SushiswapFantomFarmingMinichefMarketService,
    blockchainGatewayProvider: BlockchainGatewayProvider
) : DefaultFarmingPositionProvider(erC20Resource, stakingMarketService, blockchainGatewayProvider)