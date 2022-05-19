package io.defitrack.protocol.mstable.lending

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.lending.DefaultLendingPositionService

@Deprecated("not a lending market")
class MStablePolygonLendingPositionService(
    contractAccessorGateway: ContractAccessorGateway,
    mStablePolygonLendingMarketService: MStablePolygonLendingMarketService,
) : DefaultLendingPositionService(
    mStablePolygonLendingMarketService, contractAccessorGateway
)