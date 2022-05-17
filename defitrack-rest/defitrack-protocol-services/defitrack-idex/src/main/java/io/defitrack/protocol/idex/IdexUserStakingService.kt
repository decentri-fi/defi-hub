package io.defitrack.protocol.idex

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultUserStakingService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class IdexUserStakingService(
    erC20Resource: ERC20Resource,
    idexFarmingMarketService: IdexFarmingMarketService,
    gateway: ContractAccessorGateway
) : DefaultUserStakingService(
    erC20Resource, idexFarmingMarketService, gateway
)