package io.defitrack.protocol.sushiswap.pooling.fantom

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SushiswapService
import io.defitrack.protocol.sushiswap.pooling.DefaultSushiPoolingMarketProvider
import org.springframework.stereotype.Component

@Component
class SushiswapFantomPoolingMarketProvider(
    sushiServices: List<SushiswapService>,
) : DefaultSushiPoolingMarketProvider(sushiServices) {

    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}