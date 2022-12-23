package io.defitrack.claiming

import io.defitrack.claimable.DefaultClaimableRewardProvider
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.staking.AelinRewardMarketProvider
import org.springframework.stereotype.Component

@Component
class StakingRewardClaimableProvider(
    aelinRewardMarketProvider: AelinRewardMarketProvider,
    blockchainGatewayProvider: BlockchainGatewayProvider
) : DefaultClaimableRewardProvider(
    aelinRewardMarketProvider,
    blockchainGatewayProvider
)