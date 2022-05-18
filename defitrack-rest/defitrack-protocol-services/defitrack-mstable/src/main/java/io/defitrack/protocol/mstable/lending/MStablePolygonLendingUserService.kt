package io.defitrack.protocol.mstable.lending

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.lending.DefaultLendingUserService
import org.springframework.stereotype.Service

@Deprecated("not a lending market")
class MStablePolygonLendingUserService(
    contractAccessorGateway: ContractAccessorGateway,
    mStablePolygonLendingMarketService: MStablePolygonLendingMarketService,
) : DefaultLendingUserService(
    mStablePolygonLendingMarketService, contractAccessorGateway
)