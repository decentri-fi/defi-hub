package io.defitrack.protocol.sushiswap.pooling.polygon

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SushiswapService
import io.defitrack.protocol.sushiswap.pooling.DefaultSushiPoolingPositionProvider
import org.springframework.stereotype.Service

@Service
class SushiswapPolygonPoolingPositionProvider(
    sushiServices: List<SushiswapService>,
) : DefaultSushiPoolingPositionProvider(sushiServices) {
    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}