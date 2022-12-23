package io.defitrack.protocol.beefy.staking

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.DefaultFarmingPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class BeefyArbitrumFarmingPositionProvider(
    beefyArbitrumStakingMarketService: BeefyArbitrumFarmingMarketProvider,
    blockchainGatewayProvider: BlockchainGatewayProvider,
    erC20Resource: ERC20Resource
) : DefaultFarmingPositionProvider(erC20Resource, beefyArbitrumStakingMarketService, blockchainGatewayProvider)