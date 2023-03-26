package io.defitrack.protocol.sushiswap.pooling.polygon

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SushiswapService
import io.defitrack.protocol.sushiswap.pooling.DefaultSushiPoolingMarketProvider
import org.springframework.stereotype.Component

@Component
class SushiswapPolygonPoolingMarketProvider(
    sushiServices: List<SushiswapService>,
) : DefaultSushiPoolingMarketProvider(sushiServices) {

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}