package io.defitrack.protocol.idex

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultStakingPositionPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class IdexStakingPositionPositionService(
    erC20Resource: ERC20Resource,
    idexFarmingMarketService: IdexFarmingMarketService,
    gateway: ContractAccessorGateway
) : DefaultStakingPositionPositionService(
    erC20Resource, idexFarmingMarketService, gateway
)