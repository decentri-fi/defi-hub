package io.defitrack.protocol.compound.lending

import io.defitrack.abi.ABIResource
import io.defitrack.common.network.Network
import io.defitrack.evm.contract.ContractAccessorGateway
import io.defitrack.price.PriceResource
import io.defitrack.protocol.compound.IronBankEthereumService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class IronBankEthereumLendingMarketService(
    contractAccessorGateway: ContractAccessorGateway,
    abiResource: ABIResource,
    erC20Resource: ERC20Resource,
    compoundEthereumService: IronBankEthereumService,
    priceResource: PriceResource
) : IronBankLendingMarketService(
    contractAccessorGateway,
    abiResource,
    erC20Resource,
    compoundEthereumService,
    priceResource

) {
    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}