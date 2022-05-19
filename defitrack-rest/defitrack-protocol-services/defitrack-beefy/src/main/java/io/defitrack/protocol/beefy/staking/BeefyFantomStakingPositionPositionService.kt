package io.defitrack.protocol.beefy.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultStakingPositionPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class BeefyFantomStakingPositionPositionService(
    contractAccessorGateway: ContractAccessorGateway,
    erC20Resource: ERC20Resource,
    stakingMarketService: BeefyFantomStakingMarketService
) : DefaultStakingPositionPositionService(erC20Resource, stakingMarketService, contractAccessorGateway)