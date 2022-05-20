package io.defitrack.protocol.beefy.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultStakingPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class BeefyPolygonStakingPositionService(
    contractAccessorGateway: ContractAccessorGateway,
    polygonStakingMarketService: BeefyPolygonStakingMarketService,
    erC20Resource: ERC20Resource,
) : DefaultStakingPositionService(erC20Resource, polygonStakingMarketService, contractAccessorGateway)