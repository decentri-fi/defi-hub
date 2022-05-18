package io.defitrack.protocol.mstable.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultUserStakingService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class MStableEthereumStakingService(
    mStableEthereumStakingMarketService: MStableEthereumStakingMarketService,
    erC20Resource: ERC20Resource,
    gateway: ContractAccessorGateway
) : DefaultUserStakingService(erC20Resource, mStableEthereumStakingMarketService, gateway)