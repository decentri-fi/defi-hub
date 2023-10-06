package io.defitrack.protocol.balancer.pooling.history

import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.balancer.pooling.BalancerAZkEVMPoolingMarketProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BALANCER)
@ConditionalOnProperty(value = ["polygon-zkevm.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerZkEVMPoolingHistoryProvider(
    balancerPoolingMarketProvider: BalancerAZkEVMPoolingMarketProvider
) : BalancerPoolingHistoryProvider(balancerPoolingMarketProvider)