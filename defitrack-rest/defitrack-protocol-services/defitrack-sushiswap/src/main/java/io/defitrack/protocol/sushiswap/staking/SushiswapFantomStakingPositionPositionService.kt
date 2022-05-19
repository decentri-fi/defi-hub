package io.defitrack.protocol.sushiswap.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultStakingPositionPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class SushiswapFantomStakingPositionPositionService(
    erC20Resource: ERC20Resource,
    stakingMarketService: SushiswapFantomStakingMinichefMarketService,
    contractAccessorGateway: ContractAccessorGateway
) : DefaultStakingPositionPositionService(erC20Resource, stakingMarketService, contractAccessorGateway)