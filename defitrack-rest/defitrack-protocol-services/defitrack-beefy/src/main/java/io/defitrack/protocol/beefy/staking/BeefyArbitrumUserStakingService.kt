package io.defitrack.protocol.beefy.staking

import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.protocol.Protocol
import io.defitrack.staking.DefaultUserStakingService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class BeefyArbitrumUserStakingService(
    beefyArbitrumStakingMarketService: BeefyArbitrumStakingMarketService,
    contractAccessorGateway: ContractAccessorGateway,
    erC20Resource: ERC20Resource
) : DefaultUserStakingService(erC20Resource, beefyArbitrumStakingMarketService, contractAccessorGateway)