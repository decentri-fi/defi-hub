package io.defitrack.protocol.compound.lending

import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.market.lending.DefaultLendingPositionService
import org.springframework.stereotype.Service

@Service
class CompoundLendingPositionService(
    compoundLendingMarketService: CompoundLendingMarketProvider,
    gateway: BlockchainGatewayProvider,
) : DefaultLendingPositionService(compoundLendingMarketService, gateway)