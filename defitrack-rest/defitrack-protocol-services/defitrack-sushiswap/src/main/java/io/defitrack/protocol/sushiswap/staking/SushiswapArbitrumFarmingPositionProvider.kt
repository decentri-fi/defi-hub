package io.defitrack.protocol.sushiswap.staking

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.DefaultFarmingPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class SushiswapArbitrumFarmingPositionProvider(
    erC20Resource: ERC20Resource,
    sushiswapArbitrumStakingMinichefMarketService: SushiswapArbitrumFarmingMinichefMarketProvider,
    blockchainGatewayProvider: BlockchainGatewayProvider
) : DefaultFarmingPositionProvider(erC20Resource, sushiswapArbitrumStakingMinichefMarketService, blockchainGatewayProvider)