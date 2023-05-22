package io.defitrack.protocol.balancer.staking

import io.defitrack.protocol.balancer.pooling.BalancerOptimismPoolingMarketProvider
import org.springframework.stereotype.Component

@Component
class BalancerOptimismGaugeMarketProvider(
    poolingMarketProvider: BalancerOptimismPoolingMarketProvider
) : BalancerGaugeFarmingMarketProvider(
    poolingMarketProvider, "0x2E96068b3D5B5BAE3D7515da4A1D2E52d08A2647"
)