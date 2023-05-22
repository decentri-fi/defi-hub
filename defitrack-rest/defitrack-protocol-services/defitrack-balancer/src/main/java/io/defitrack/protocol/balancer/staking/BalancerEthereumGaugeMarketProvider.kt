package io.defitrack.protocol.balancer.staking

import io.defitrack.protocol.balancer.pooling.BalancerEthereumPoolingMarketProvider
import org.springframework.stereotype.Component

@Component
class BalancerEthereumGaugeMarketProvider(
    balancerEthereumPoolingMarketProvider: BalancerEthereumPoolingMarketProvider
) : BalancerGaugeFarmingMarketProvider(
    balancerEthereumPoolingMarketProvider, "0x4e7bbd911cf1efa442bc1b2e9ea01ffe785412ec"
)