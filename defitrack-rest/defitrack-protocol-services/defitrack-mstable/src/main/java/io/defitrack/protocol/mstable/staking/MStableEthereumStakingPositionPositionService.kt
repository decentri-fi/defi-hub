package io.defitrack.protocol.mstable.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultStakingPositionPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class MStableEthereumStakingPositionPositionService(
    mStableEthereumStakingMarketService: MStableEthereumStakingMarketService,
    erC20Resource: ERC20Resource,
    gateway: ContractAccessorGateway
) : DefaultStakingPositionPositionService(erC20Resource, mStableEthereumStakingMarketService, gateway)