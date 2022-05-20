package io.defitrack.protocol.beefy.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultStakingPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class BeefyFantomStakingPositionService(
    contractAccessorGateway: ContractAccessorGateway,
    erC20Resource: ERC20Resource,
    stakingMarketService: BeefyFantomStakingMarketService
) : DefaultStakingPositionService(erC20Resource, stakingMarketService, contractAccessorGateway)