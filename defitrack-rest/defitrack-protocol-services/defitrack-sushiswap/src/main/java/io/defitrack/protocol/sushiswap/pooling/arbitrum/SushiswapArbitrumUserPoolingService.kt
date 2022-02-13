package io.defitrack.protocol.sushiswap.pooling.arbitrum

import io.defitrack.common.network.Network
import io.defitrack.protocol.Protocol
import io.defitrack.protocol.SushiswapService
import io.defitrack.protocol.sushiswap.pooling.DefaultSushiUserPoolingService
import org.springframework.stereotype.Service

@Service
class SushiswapArbitrumUserPoolingService(
    sushiServices: List<SushiswapService>,
) : DefaultSushiUserPoolingService(sushiServices) {

    override fun getProtocol(): Protocol {
        return Protocol.SUSHISWAP
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}