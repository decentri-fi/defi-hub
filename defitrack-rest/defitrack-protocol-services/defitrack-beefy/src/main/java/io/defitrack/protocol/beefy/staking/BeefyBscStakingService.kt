package io.defitrack.protocol.beefy.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultUserStakingService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class BeefyBscStakingService(
    contractAccessorGateway: ContractAccessorGateway,
    stakingMarketService: BeefyBscStakingMarketService,
    erC20Resource: ERC20Resource
) : DefaultUserStakingService(erC20Resource, stakingMarketService, contractAccessorGateway)
