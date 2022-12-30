package io.defitrack.protocol.compound.lending

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.PriceResource
import io.defitrack.protocol.compound.IronBankEthereumService
import org.springframework.stereotype.Component

@Component
class IronBankEthereumLendingMarketProvider(
    abiResource: ABIResource,
    compoundEthereumService: IronBankEthereumService,
    priceResource: PriceResource
) : IronBankLendingMarketProvider(
    abiResource,
    compoundEthereumService,
    priceResource
) {
    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}