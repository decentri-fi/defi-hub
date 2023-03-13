package io.defitrack.protocol.mstable.lending

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.AbstractLendingPositionProvider

@Deprecated("not a lending market")
class MStablePolygonLendingPositionProvider(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    mStablePolygonLendingMarketService: MStablePolygonLendingMarketProvider,
) : AbstractLendingPositionProvider(
    mStablePolygonLendingMarketService, blockchainGatewayProvider
)