package io.defitrack.farming

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.DefaultFarmingPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class SpookyFarmingPositionProvider(
    spookyStakingMarketService: SpookyFarmingMarketService,
    blockchainGatewayProvider: BlockchainGatewayProvider,
    erC20Resource: ERC20Resource,
) : DefaultFarmingPositionProvider(erC20Resource, spookyStakingMarketService, blockchainGatewayProvider)