package io.defitrack.protocol.dodo.pooling

import io.defitrack.common.network.Network
import io.defitrack.price.PriceResource
import io.defitrack.protocol.DodoEthereumGraphProvider
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class DodoEthereumPoolingMarketProvider(
    erC20Resource: ERC20Resource,
    dodoEthereumGraphProvider: DodoEthereumGraphProvider,
    priceResource: PriceResource,
) : DodoPoolingMarketProvider(erC20Resource, dodoEthereumGraphProvider, priceResource) {
    override fun getProtocol(): Protocol {
        return Protocol.DODO
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}