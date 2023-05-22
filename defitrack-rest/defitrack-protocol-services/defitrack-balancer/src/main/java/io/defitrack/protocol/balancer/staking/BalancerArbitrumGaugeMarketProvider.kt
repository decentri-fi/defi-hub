package io.defitrack.protocol.balancer.staking

import io.defitrack.protocol.balancer.pooling.BalancerArbitrumPoolingMarketProvider
import org.springframework.stereotype.Component

@Component
class BalancerArbitrumGaugeMarketProvider(
    poolingMarketProvider: BalancerArbitrumPoolingMarketProvider
) : BalancerGaugeFarmingMarketProvider(
    poolingMarketProvider, "0xb08E16cFc07C684dAA2f93C70323BAdb2A6CBFd2"
)