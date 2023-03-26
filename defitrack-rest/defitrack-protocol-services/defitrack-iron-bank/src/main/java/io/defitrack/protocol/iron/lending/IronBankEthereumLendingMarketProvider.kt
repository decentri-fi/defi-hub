package io.defitrack.protocol.iron.lending

import io.defitrack.common.network.Network
import io.defitrack.protocol.compound.IronBankEthereumService
import org.springframework.stereotype.Component

@Component
class IronBankEthereumLendingMarketProvider(
    compoundEthereumService: IronBankEthereumService,
) : IronBankLendingMarketProvider(compoundEthereumService) {
    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}