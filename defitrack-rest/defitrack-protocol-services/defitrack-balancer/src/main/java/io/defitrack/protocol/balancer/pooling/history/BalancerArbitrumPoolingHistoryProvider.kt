package io.defitrack.protocol.balancer.pooling.history

import io.defitrack.protocol.balancer.pooling.BalancerAZkEVMPoolingMarketProvider
import io.defitrack.protocol.balancer.pooling.BalancerArbitrumPoolingMarketProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["arbitrum.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerArbitrumPoolingHistoryProvider(
    balancerPoolingMarketProvider: BalancerArbitrumPoolingMarketProvider
) : BalancerPoolingHistoryProvider(balancerPoolingMarketProvider)
