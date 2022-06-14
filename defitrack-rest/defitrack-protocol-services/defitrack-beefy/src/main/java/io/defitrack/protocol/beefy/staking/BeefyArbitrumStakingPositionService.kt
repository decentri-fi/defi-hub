package io.defitrack.protocol.beefy.staking

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.staking.DefaultStakingPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class BeefyArbitrumStakingPositionService(
    beefyArbitrumStakingMarketService: BeefyArbitrumStakingMarketService,
    blockchainGatewayProvider: BlockchainGatewayProvider,
    erC20Resource: ERC20Resource
) : DefaultStakingPositionService(erC20Resource, beefyArbitrumStakingMarketService, blockchainGatewayProvider)