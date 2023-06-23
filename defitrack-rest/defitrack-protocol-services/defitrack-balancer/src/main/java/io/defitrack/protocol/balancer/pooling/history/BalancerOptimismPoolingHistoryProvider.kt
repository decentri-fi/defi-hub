package io.defitrack.protocol.balancer.pooling.history

import io.defitrack.protocol.balancer.pooling.BalancerAZkEVMPoolingMarketProvider
import io.defitrack.protocol.balancer.pooling.BalancerOptimismPoolingMarketProvider
import org.springframework.stereotype.Component

@Component
class BalancerOptimismPoolingHistoryProvider(
    balancerPoolingMarketProvider: BalancerOptimismPoolingMarketProvider
) : BalancerPoolingHistoryProvider(balancerPoolingMarketProvider)
