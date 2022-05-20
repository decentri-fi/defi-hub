package io.defitrack.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class SpritFantomStakingPositionService(
    erC20Resource: ERC20Resource,
    contractAccessorGateway: ContractAccessorGateway,
    spiritFantomStakingMarketService: SpiritFantomStakingMarketService
) : DefaultStakingPositionService(
    erC20Resource, spiritFantomStakingMarketService, contractAccessorGateway
)