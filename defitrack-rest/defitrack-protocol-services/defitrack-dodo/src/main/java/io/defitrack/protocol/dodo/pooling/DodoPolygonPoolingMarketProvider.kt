package io.defitrack.protocol.dodo.pooling

import io.defitrack.common.network.Network
import io.defitrack.protocol.DodoPolygonGraphProvider
import io.defitrack.protocol.Protocol
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Service

@Service
class DodoPolygonPoolingMarketProvider(
    erC20Resource: ERC20Resource,
    dodoPolygonGraphProvider: DodoPolygonGraphProvider,
) : DodoPoolingMarketProvider(erC20Resource, dodoPolygonGraphProvider) {

    override fun getProtocol(): Protocol {
        return Protocol.DODO
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}