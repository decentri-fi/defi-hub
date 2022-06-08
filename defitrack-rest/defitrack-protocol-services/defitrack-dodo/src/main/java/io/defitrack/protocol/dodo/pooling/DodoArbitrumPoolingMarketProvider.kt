package io.defitrack.protocol.dodo.pooling

import io.defitrack.common.network.Network
import io.defitrack.protocol.DodoArbitrumGraphProvider
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class DodoArbitrumPoolingMarketProvider(
    erC20Resource: ERC20Resource,
    dodoGraphProvider: DodoArbitrumGraphProvider,
) : DodoPoolingMarketProvider(erC20Resource, dodoGraphProvider) {

    override fun getProtocol(): Protocol {
        return Protocol.DODO
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}