package io.defitrack.protocol.sushiswap.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultUserStakingService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class SushiswapPolygonUserStakingService(
    erC20Resource: ERC20Resource,
    stakingMarketService: SushiswapPolygonStakingMinichefMarketService,
    contractAccessorGateway: ContractAccessorGateway
) : DefaultUserStakingService(erC20Resource, stakingMarketService, contractAccessorGateway)