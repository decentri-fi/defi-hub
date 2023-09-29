package io.defitrack.protocol.balancer.staking

import io.defitrack.protocol.balancer.pooling.BalancerOptimismPoolingMarketProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["optimism.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerOptimismGaugeMarketProvider(
    poolingMarketProvider: BalancerOptimismPoolingMarketProvider
) : BalancerGaugeFarmingMarketProvider(
    poolingMarketProvider, "0x2E96068b3D5B5BAE3D7515da4A1D2E52d08A2647"
)