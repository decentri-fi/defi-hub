package io.defitrack.protocol.beefy.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultUserStakingService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class BeefyPolygonUserStakingService(
    contractAccessorGateway: ContractAccessorGateway,
    polygonStakingMarketService: BeefyPolygonStakingMarketService,
    erC20Resource: ERC20Resource,
) : DefaultUserStakingService(erC20Resource, polygonStakingMarketService, contractAccessorGateway)