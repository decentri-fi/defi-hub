package io.defitrack.protocol.mstable.staking

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.farming.DefaultFarmingPositionProvider
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class MStableEthereumFarmingPositionProvider(
    mStableEthereumStakingMarketService: MStableEthereumFarmingMarketService,
    erC20Resource: ERC20Resource,
    gateway: BlockchainGatewayProvider
) : DefaultFarmingPositionProvider(erC20Resource, mStableEthereumStakingMarketService, gateway)