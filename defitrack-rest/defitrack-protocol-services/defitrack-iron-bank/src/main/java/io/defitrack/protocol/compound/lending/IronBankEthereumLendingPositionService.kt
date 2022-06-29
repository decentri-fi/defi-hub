package io.defitrack.protocol.compound.lending

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.DefaultLendingPositionService
import org.springframework.stereotype.Service

@Service
class IronBankEthereumLendingPositionService(
    lendingMarketService: IronBankEthereumLendingMarketProvider,
    gateway: BlockchainGatewayProvider,
) : DefaultLendingPositionService(lendingMarketService, gateway)