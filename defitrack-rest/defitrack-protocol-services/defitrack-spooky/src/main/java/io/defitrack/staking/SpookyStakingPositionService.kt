package io.defitrack.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class SpookyStakingPositionService(
    spookyStakingMarketService: SpookyStakingMarketService,
    contractAccessorGateway: ContractAccessorGateway,
    erC20Resource: ERC20Resource,
) : DefaultStakingPositionService(erC20Resource, spookyStakingMarketService, contractAccessorGateway)