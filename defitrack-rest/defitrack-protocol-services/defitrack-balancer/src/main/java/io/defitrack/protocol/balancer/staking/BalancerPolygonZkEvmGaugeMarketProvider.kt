package io.defitrack.protocol.balancer.staking

import io.defitrack.protocol.balancer.pooling.BalancerAZkEVMPoolingMarketProvider
import org.springframework.stereotype.Component

//@Component
class BalancerPolygonZkEvmGaugeMarketProvider(
    balancerPoolingMarketProvider: BalancerAZkEVMPoolingMarketProvider
) : BalancerGaugeFarmingMarketProvider(
    balancerPoolingMarketProvider, "0x2498A2B0d6462d2260EAC50aE1C3e03F4829BA95"
)