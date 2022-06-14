package io.defitrack.protocol.idex

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.staking.DefaultStakingPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class IdexStakingPositionService(
    erC20Resource: ERC20Resource,
    idexFarmingMarketService: IdexFarmingMarketService,
    gateway: BlockchainGatewayProvider
) : DefaultStakingPositionService(
    erC20Resource, idexFarmingMarketService, gateway
)