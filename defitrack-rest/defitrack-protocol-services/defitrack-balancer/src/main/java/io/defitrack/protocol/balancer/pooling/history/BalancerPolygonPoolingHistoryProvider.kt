package io.defitrack.protocol.balancer.pooling.history

import io.defitrack.protocol.balancer.pooling.BalancerPolygonPoolingMarketProvider
import org.springframework.stereotype.Component

@Component
class BalancerPolygonPoolingHistoryProvider(
    balancerPoolingMarketProvider: BalancerPolygonPoolingMarketProvider
) : BalancerPoolingHistoryProvider(balancerPoolingMarketProvider)
