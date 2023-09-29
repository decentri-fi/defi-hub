package io.defitrack.protocol.balancer.staking

import io.defitrack.protocol.balancer.pooling.BalancerArbitrumPoolingMarketProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["arbitrum.enabled", "uniswapv3.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerArbitrumGaugeMarketProvider(
    poolingMarketProvider: BalancerArbitrumPoolingMarketProvider
) : BalancerGaugeFarmingMarketProvider(
    poolingMarketProvider, "0xb08E16cFc07C684dAA2f93C70323BAdb2A6CBFd2"
)