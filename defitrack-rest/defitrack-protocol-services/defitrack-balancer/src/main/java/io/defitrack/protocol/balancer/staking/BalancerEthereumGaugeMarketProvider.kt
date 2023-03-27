package io.defitrack.protocol.balancer.staking

import io.defitrack.protocol.balancer.pooling.BalancerEthereumPoolingMarketProvider
import org.springframework.stereotype.Component

@Component
class BalancerEthereumGaugeMarketProvider(
    balancerPoolingMarketProvider: BalancerEthereumPoolingMarketProvider
) : BalancerGaugeFarmingMarketProvider(
    balancerPoolingMarketProvider, "0x4e7bbd911cf1efa442bc1b2e9ea01ffe785412ec"
)