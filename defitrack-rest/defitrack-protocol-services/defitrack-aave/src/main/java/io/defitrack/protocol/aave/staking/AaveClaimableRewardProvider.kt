package io.defitrack.protocol.aave.staking

import io.defitrack.claimable.DefaultClaimableRewardProvider
import io.defitrack.evm.contract.BlockchainGatewayProvider
import org.springframework.stereotype.Component

@Component
class AaveClaimableRewardProvider(
    aaveStakingMarketProvider: AaveStakingMarketProvider,
    blockchainGatewayProvider: BlockchainGatewayProvider
) : DefaultClaimableRewardProvider(aaveStakingMarketProvider, blockchainGatewayProvider)