package io.defitrack.protocol.quickswap.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultUserStakingService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class QuickswapUserStakingService(
    contractAccessorGateway: ContractAccessorGateway,
    quickswapStakingMarketService: QuickswapStakingMarketService,
    erC20Resource: ERC20Resource,
) : DefaultUserStakingService(erC20Resource, quickswapStakingMarketService, contractAccessorGateway)