package io.defitrack.protocol.sushiswap.staking

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.staking.DefaultStakingPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class SushiswapArbitrumStakingPositionService(
    erC20Resource: ERC20Resource,
    sushiswapArbitrumStakingMinichefMarketService: SushiswapArbitrumStakingMinichefMarketService,
    blockchainGatewayProvider: BlockchainGatewayProvider
) : DefaultStakingPositionService(erC20Resource, sushiswapArbitrumStakingMinichefMarketService, blockchainGatewayProvider)