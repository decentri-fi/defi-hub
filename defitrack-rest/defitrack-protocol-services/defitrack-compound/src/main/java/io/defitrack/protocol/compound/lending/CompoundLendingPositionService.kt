package io.defitrack.protocol.compound.lending

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.lending.DefaultLendingPositionService
import org.springframework.stereotype.Service

@Service
class CompoundLendingPositionService(
    compoundLendingMarketService: CompoundLendingMarketService,
    gateway: ContractAccessorGateway,
) : DefaultLendingPositionService(compoundLendingMarketService, gateway)