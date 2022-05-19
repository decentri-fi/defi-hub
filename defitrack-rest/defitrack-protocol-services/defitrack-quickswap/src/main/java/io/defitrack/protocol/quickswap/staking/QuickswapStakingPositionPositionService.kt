package io.defitrack.protocol.quickswap.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultStakingPositionPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class QuickswapStakingPositionPositionService(
    contractAccessorGateway: ContractAccessorGateway,
    quickswapStakingMarketService: QuickswapStakingMarketService,
    erC20Resource: ERC20Resource,
) : DefaultStakingPositionPositionService(erC20Resource, quickswapStakingMarketService, contractAccessorGateway)