package io.defitrack.protocol.aave.v2.lending.position

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.DefaultLendingPositionService
import io.defitrack.protocol.aave.v2.lending.market.AaveV2PolygonLendingMarketProvider
import org.springframework.stereotype.Service

@Service
class AaveV2PolygonLendingPositionProvider(
    aaveV2PolygonLendingMarketProvider: AaveV2PolygonLendingMarketProvider,
    blockchainGatewayProvider: BlockchainGatewayProvider
) : DefaultLendingPositionService(
    aaveV2PolygonLendingMarketProvider, blockchainGatewayProvider
)