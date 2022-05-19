package io.defitrack.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class SpritFantomStakingPositionPositionService(
    erC20Resource: ERC20Resource,
    contractAccessorGateway: ContractAccessorGateway,
    spiritFantomStakingMarketService: SpiritFantomStakingMarketService
) : DefaultStakingPositionPositionService(
    erC20Resource, spiritFantomStakingMarketService, contractAccessorGateway
)