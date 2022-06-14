package io.defitrack.protocol.mstable.staking

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.staking.DefaultStakingPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class MStableEthereumStakingPositionService(
    mStableEthereumStakingMarketService: MStableEthereumStakingMarketService,
    erC20Resource: ERC20Resource,
    gateway: BlockchainGatewayProvider
) : DefaultStakingPositionService(erC20Resource, mStableEthereumStakingMarketService, gateway)