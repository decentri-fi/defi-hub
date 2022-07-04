package io.defitrack.protocol.aave.v2.lending.position

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.DefaultLendingPositionService
import io.defitrack.protocol.aave.v2.lending.market.AaveV2MainnetLendingMarketProvider
import org.springframework.stereotype.Service

@Service
class AaveV2MainnetLendingPositionProvider(
    aaveLendingMarketProvider: AaveV2MainnetLendingMarketProvider,
    blockchainGatewayProvider: BlockchainGatewayProvider
) : DefaultLendingPositionService(
    aaveLendingMarketProvider, blockchainGatewayProvider
)