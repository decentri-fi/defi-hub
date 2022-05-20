package io.defitrack.protocol.quickswap.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultStakingPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class QuickswapDualStakingPositionService(
    contractAccessorGateway: ContractAccessorGateway,
    quickswapStakingMarketService: QuickswapDualStakingMarketService,
    erC20Resource: ERC20Resource,
) : DefaultStakingPositionService(erC20Resource, quickswapStakingMarketService, contractAccessorGateway)