package io.defitrack.protocol.sushiswap.staking

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.staking.DefaultStakingPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class SushiswapPolygonStakingPositionService(
    erC20Resource: ERC20Resource,
    stakingMarketService: SushiswapPolygonStakingMinichefMarketService,
    blockchainGatewayProvider: BlockchainGatewayProvider
) : DefaultStakingPositionService(erC20Resource, stakingMarketService, blockchainGatewayProvider)