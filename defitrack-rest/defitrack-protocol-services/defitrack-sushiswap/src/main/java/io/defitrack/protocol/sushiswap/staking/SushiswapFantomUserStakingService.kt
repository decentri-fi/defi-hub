package io.defitrack.protocol.sushiswap.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultUserStakingService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class SushiswapFantomUserStakingService(
    erC20Resource: ERC20Resource,
    stakingMarketService: SushiswapFantomStakingMinichefMarketService,
    contractAccessorGateway: ContractAccessorGateway
) : DefaultUserStakingService(erC20Resource, stakingMarketService, contractAccessorGateway)