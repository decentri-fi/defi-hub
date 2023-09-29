package io.defitrack.protocol.balancer.pooling.history

import io.defitrack.protocol.balancer.pooling.BalancerPolygonPoolingMarketProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["polygon.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerPolygonPoolingHistoryProvider(
    balancerPoolingMarketProvider: BalancerPolygonPoolingMarketProvider
) : BalancerPoolingHistoryProvider(balancerPoolingMarketProvider)
