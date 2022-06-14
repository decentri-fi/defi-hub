package io.defitrack.staking

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class SpookyStakingPositionService(
    spookyStakingMarketService: SpookyStakingMarketService,
    blockchainGatewayProvider: BlockchainGatewayProvider,
    erC20Resource: ERC20Resource,
) : DefaultStakingPositionService(erC20Resource, spookyStakingMarketService, blockchainGatewayProvider)