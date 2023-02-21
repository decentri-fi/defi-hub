package io.defitrack.protocol.sushiswap.pooling.avalanche

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SushiswapService
import io.defitrack.protocol.sushiswap.pooling.DefaultSushiPoolingMarketProvider
import org.springframework.stereotype.Component

@Component
class SushiswapAvalanchePoolingMarketProvider(
    sushiServices: List<SushiswapService>,
) : DefaultSushiPoolingMarketProvider(sushiServices) {

    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override fun getNetwork(): Network {
        return Network.AVALANCHE
    }
}