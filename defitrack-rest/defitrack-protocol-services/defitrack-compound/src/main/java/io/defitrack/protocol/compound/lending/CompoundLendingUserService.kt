package io.defitrack.protocol.compound.lending

import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.lending.DefaultLendingUserService
import org.springframework.stereotype.Service

@Service
class CompoundLendingUserService(
    compoundLendingMarketService: CompoundLendingMarketService,
    gateway: ContractAccessorGateway,
) : DefaultLendingUserService(compoundLendingMarketService, gateway)