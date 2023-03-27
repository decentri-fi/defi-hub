package io.defitrack.pooling

import io.defitrack.common.network.Network
import io.defitrack.protocol.StargateArbitrumService
import io.defitrack.protocol.StargateBinanceService
import org.springframework.stereotype.Component

@Component
class BinanceStargatePoolingMarketProvider(
    stargateArbitrumService: StargateBinanceService
) : StargatePoolingMarketProvider(
    stargateArbitrumService
) {
    override fun getNetwork(): Network {
        return Network.BINANCE
    }
}