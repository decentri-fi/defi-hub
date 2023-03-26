package io.defitrack.farming

import io.defitrack.common.network.Network
import io.defitrack.protocol.StargateBinanceService
import org.springframework.stereotype.Component

@Component
class StargateBinanceFarmingProvider(
    stargateService: StargateBinanceService,
) : StargateFarmingMarketProvider(
    stargateService
) {
    override fun getNetwork(): Network {
        return Network.BINANCE
    }
}