package io.defitrack.protocol.sushiswap.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultStakingPositionPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class SushiswapArbitrumStakingPositionPositionService(
    erC20Resource: ERC20Resource,
    sushiswapArbitrumStakingMinichefMarketService: SushiswapArbitrumStakingMinichefMarketService,
    contractAccessorGateway: ContractAccessorGateway
) : DefaultStakingPositionPositionService(erC20Resource, sushiswapArbitrumStakingMinichefMarketService, contractAccessorGateway)