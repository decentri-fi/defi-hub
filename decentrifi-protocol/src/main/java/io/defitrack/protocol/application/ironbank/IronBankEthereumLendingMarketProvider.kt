package io.defitrack.protocol.application.ironbank

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.ironbank.IronBankEthereumService
import io.defitrack.protocol.application.ironbank.lending.IronBankLendingMarketProvider
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.IRON_BANK)
class IronBankEthereumLendingMarketProvider(
    compoundEthereumService: IronBankEthereumService,
) : IronBankLendingMarketProvider(compoundEthereumService) {
    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}