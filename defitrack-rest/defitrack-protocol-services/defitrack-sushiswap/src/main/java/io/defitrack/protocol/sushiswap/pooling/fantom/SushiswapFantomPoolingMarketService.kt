package io.defitrack.protocol.sushiswap.pooling.fantom

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SushiswapService
import io.defitrack.protocol.sushiswap.pooling.DefaultSushiPoolingMarketService
import io.defitrack.token.ERC20Resource
import org.springframework.stereotype.Component

@Component
class SushiswapFantomPoolingMarketService(
    sushiServices: List<SushiswapService>,
    erC20Resource: ERC20Resource
) : DefaultSushiPoolingMarketService(sushiServices, erC20Resource) {

    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override fun getNetwork(): Network {
        return Network.FANTOM
    }
}