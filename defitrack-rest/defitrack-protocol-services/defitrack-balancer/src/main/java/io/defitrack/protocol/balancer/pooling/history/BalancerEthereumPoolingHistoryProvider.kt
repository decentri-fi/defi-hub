package io.defitrack.protocol.balancer.pooling.history

import io.defitrack.protocol.balancer.pooling.BalancerEthereumPoolingMarketProvider
import org.springframework.stereotype.Component

@Component
class BalancerEthereumPoolingHistoryProvider(
    balancerPoolingMarketProvider: BalancerEthereumPoolingMarketProvider
) : BalancerPoolingHistoryProvider(balancerPoolingMarketProvider)
