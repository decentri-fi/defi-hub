package io.defitrack.protocol.balancer.pooling.history

import io.defitrack.protocol.balancer.pooling.BalancerAZkEVMPoolingMarketProvider
import io.defitrack.protocol.balancer.pooling.BalancerOptimismPoolingMarketProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["optimism.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerOptimismPoolingHistoryProvider(
    balancerPoolingMarketProvider: BalancerOptimismPoolingMarketProvider
) : BalancerPoolingHistoryProvider(balancerPoolingMarketProvider)
