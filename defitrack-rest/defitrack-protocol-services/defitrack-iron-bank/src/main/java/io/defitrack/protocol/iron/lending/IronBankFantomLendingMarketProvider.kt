package io.defitrack.protocol.iron.lending

import io.defitrack.common.network.Network
import io.defitrack.protocol.compound.IronBankFantomService
import org.springframework.stereotype.Component

@Component
class IronBankFantomLendingMarketProvider(compoundEthereumService: IronBankFantomService) :
    IronBankLendingMarketProvider(compoundEthereumService) {
    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}