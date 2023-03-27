package io.defitrack.protocol.balancer.staking

import io.defitrack.protocol.balancer.pooling.BalancerEthereumPoolingMarketProvider
import org.springframework.stereotype.Component

@Component
class BalancerPolygonGaugeMarketProvider(
    balancerPoolingMarketProvider: BalancerEthereumPoolingMarketProvider
) : BalancerGaugeFarmingMarketProvider(
    balancerPoolingMarketProvider, "0x3b8ca519122cdd8efb272b0d3085453404b25bd0"
)