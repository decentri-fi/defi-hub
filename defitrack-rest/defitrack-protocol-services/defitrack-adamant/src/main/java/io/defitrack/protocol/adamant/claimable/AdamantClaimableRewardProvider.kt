package io.defitrack.protocol.adamant.claimable

import io.defitrack.claimable.DefaultClaimableRewardProvider
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.protocol.adamant.staking.AdamantVaultMarketProvider
import org.springframework.stereotype.Service

@Service
class AdamantClaimableRewardProvider(
    adamantVaultMarketProvider: AdamantVaultMarketProvider,
    blockchainGatewayProvider: BlockchainGatewayProvider
) : DefaultClaimableRewardProvider(
    adamantVaultMarketProvider,
    blockchainGatewayProvider
)