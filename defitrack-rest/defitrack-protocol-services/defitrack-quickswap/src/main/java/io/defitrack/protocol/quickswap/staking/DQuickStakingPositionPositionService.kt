package io.defitrack.protocol.quickswap.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultStakingPositionPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class DQuickStakingPositionPositionService(
    dQuickStakingMarketService: DQuickStakingMarketService,
    contractAccessorGateway: ContractAccessorGateway,
    erC20Resource: ERC20Resource
) : DefaultStakingPositionPositionService(erC20Resource, dQuickStakingMarketService, contractAccessorGateway)