package io.defitrack.protocol.balancer.pooling.history

import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.balancer.pooling.BalancerEthereumPoolingMarketProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BALANCER)
@ConditionalOnProperty(value = ["ethereum.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerEthereumPoolingHistoryProvider(
    balancerPoolingMarketProvider: BalancerEthereumPoolingMarketProvider
) : BalancerPoolingHistoryProvider(balancerPoolingMarketProvider)
