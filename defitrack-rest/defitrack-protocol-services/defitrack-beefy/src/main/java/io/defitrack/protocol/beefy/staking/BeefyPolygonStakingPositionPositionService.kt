package io.defitrack.protocol.beefy.staking

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.staking.DefaultStakingPositionPositionService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class BeefyPolygonStakingPositionPositionService(
    contractAccessorGateway: ContractAccessorGateway,
    polygonStakingMarketService: BeefyPolygonStakingMarketService,
    erC20Resource: ERC20Resource,
) : DefaultStakingPositionPositionService(erC20Resource, polygonStakingMarketService, contractAccessorGateway)