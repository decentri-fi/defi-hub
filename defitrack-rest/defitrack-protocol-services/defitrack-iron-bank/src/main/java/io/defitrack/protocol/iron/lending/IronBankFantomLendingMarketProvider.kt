package io.defitrack.protocol.iron.lending

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.price.PriceResource
import io.defitrack.protocol.compound.IronBankFantomService
import org.springframework.stereotype.Component

@Component
class IronBankFantomLendingMarketProvider(
    abiResource: ABIResource,
    compoundEthereumService: IronBankFantomService,
    priceResource: PriceResource
) : IronBankLendingMarketProvider(
    abiResource,
    compoundEthereumService,
    priceResource
) {
    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}