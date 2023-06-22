package io.defitrack.protocol.balancer.pooling.history

import io.defitrack.protocol.balancer.pooling.BalancerAZkEVMPoolingMarketProvider
import io.defitrack.protocol.balancer.pooling.BalancerArbitrumPoolingMarketProvider
import org.springframework.stereotype.Component

@Component
class BalancerArbitrumPoolingHistoryProvider(
    balancerPoolingMarketProvider: BalancerArbitrumPoolingMarketProvider
) : BalancerPoolingHistoryProvider(balancerPoolingMarketProvider)
