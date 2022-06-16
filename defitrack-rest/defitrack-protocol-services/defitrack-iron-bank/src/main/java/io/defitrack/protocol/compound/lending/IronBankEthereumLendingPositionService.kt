package io.defitrack.protocol.compound.lending

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.DefaultLendingPositionService
import org.springframework.stereotype.Service

@Service
class IronBankEthereumLendingPositionService(
    lendingMarketService: IronBankEthereumLendingMarketService,
    gateway: BlockchainGatewayProvider,
) : DefaultLendingPositionService(lendingMarketService, gateway)