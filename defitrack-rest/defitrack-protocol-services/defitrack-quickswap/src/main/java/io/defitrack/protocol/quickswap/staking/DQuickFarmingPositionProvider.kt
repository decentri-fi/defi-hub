package io.defitrack.protocol.quickswap.staking

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.DefaultFarmingPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class DQuickFarmingPositionProvider(
    dQuickStakingMarketService: DQuickFarmingMarketService,
    blockchainGatewayProvider: BlockchainGatewayProvider,
    erC20Resource: ERC20Resource
) : DefaultFarmingPositionProvider(erC20Resource, dQuickStakingMarketService, blockchainGatewayProvider)