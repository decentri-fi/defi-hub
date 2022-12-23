package io.defitrack.protocol.compound.lending

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.BlockchainGatewayProvider
import io.defitrack.price.PriceResource
import io.defitrack.protocol.compound.IronBankFantomService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class IronBankFantomLendingMarketProvider(
    blockchainGatewayProvider: BlockchainGatewayProvider,
    abiResource: ABIResource,
    erC20Resource: ERC20Resource,
    compoundEthereumService: IronBankFantomService,
    priceResource: PriceResource
) : IronBankLendingMarketProvider(
    blockchainGatewayProvider,
    abiResource,
    erC20Resource,
    compoundEthereumService,
    priceResource

) {
    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}