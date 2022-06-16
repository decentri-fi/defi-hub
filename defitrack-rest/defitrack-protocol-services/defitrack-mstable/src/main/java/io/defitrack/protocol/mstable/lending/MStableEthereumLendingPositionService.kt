package io.defitrack.protocol.mstable.lending

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.DefaultLendingPositionService

@Deprecated("not a lending market")
class MStableEthereumLendingPositionService(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    lendingMarkteService: MStableEthereumLendingMarketService,
) : DefaultLendingPositionService(
    lendingMarkteService, blockchainGatewayProvider
)