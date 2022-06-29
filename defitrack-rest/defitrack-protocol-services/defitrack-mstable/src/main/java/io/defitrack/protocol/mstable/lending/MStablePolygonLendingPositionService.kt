package io.defitrack.protocol.mstable.lending

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.DefaultLendingPositionService

@Deprecated("not a lending market")
class MStablePolygonLendingPositionService(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    mStablePolygonLendingMarketService: MStablePolygonLendingMarketProvider,
) : DefaultLendingPositionService(
    mStablePolygonLendingMarketService, blockchainGatewayProvider
)