package io.defitrack.protocol.curve.staking

import io.defitrack.claimable.DefaultClaimableRewardProvider
import io.defitrack.evm.contract.BlockchainGatewayProvider
import org.springframework.stereotype.Service

@Service
class CurveClaimableRewardProvider(
    curveEthereumFarmingMarketProvider: CurveEthereumFarmingMarketProvider,
    blockchainGatewayProvider: BlockchainGatewayProvider
) : DefaultClaimableRewardProvider(
    curveEthereumFarmingMarketProvider,
    blockchainGatewayProvider
)