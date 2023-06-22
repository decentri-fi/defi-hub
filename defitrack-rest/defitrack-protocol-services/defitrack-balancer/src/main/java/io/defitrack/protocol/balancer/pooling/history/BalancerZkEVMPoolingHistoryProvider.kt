package io.defitrack.protocol.balancer.pooling.history

import io.defitrack.protocol.balancer.pooling.BalancerAZkEVMPoolingMarketProvider
import org.springframework.stereotype.Component

@Component
class BalancerZkEVMPoolingHistoryProvider(
    balancerPoolingMarketProvider: BalancerAZkEVMPoolingMarketProvider
) : BalancerPoolingHistoryProvider(balancerPoolingMarketProvider)
