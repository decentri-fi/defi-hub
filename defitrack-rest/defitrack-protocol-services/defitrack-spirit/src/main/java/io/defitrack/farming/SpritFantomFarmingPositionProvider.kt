package io.defitrack.farming

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.DefaultFarmingPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class SpritFantomFarmingPositionProvider(
    erC20Resource: ERC20Resource,
    blockchainGatewayProvider: BlockchainGatewayProvider,
    spiritFantomStakingMarketService: SpiritFantomFarmingMarketService
) : DefaultFarmingPositionProvider(
    erC20Resource, spiritFantomStakingMarketService, blockchainGatewayProvider
)