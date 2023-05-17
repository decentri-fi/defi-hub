package io.defitrack.protocol.dodo.pooling

import io.defitrack.common.network.Network
import io.defitrack.price.PriceResource
import io.defitrack.protocol.DodoArbitrumGraphProvider
import io.defitrack.protocol.Protocol
import org.springframework.stereotype.Service

@Service
class DodoArbitrumPoolingMarketProvider(
    dodoGraphProvider: DodoArbitrumGraphProvider,
) : DodoPoolingMarketProvider(dodoGraphProvider) {

    override fun getProtocol(): Protocol {
        return Protocol.DODO
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}