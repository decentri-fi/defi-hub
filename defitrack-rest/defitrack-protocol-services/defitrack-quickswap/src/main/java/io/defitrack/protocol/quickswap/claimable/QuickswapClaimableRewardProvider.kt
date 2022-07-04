package io.defitrack.protocol.quickswap.claimable

import io.defitrack.claimable.DefaultClaimableRewardProvider
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.quickswap.staking.QuickswapFarmingMarketProvider
import org.springframework.stereotype.Service

@Service
class QuickswapClaimableRewardProvider(
    quickswapFarmingMarketProvider: QuickswapFarmingMarketProvider,
    blockchainGatewayProvider: BlockchainGatewayProvider
) : DefaultClaimableRewardProvider(
    quickswapFarmingMarketProvider, blockchainGatewayProvider
)