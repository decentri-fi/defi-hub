package io.defitrack.protocol.balancer.pooling

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.balancer.contract.BalancerService
import io.defitrack.protocol.balancer.pooling.history.BalancerPoolingHistoryProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BALANCER)
@ConditionalOnProperty(value = ["polygon-zkevm.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerAZkEVMPoolingMarketProvider(
    balancerService: BalancerService,
    balancerPoolingHistoryProvider: BalancerPoolingHistoryProvider
) : BalancerPoolingMarketProvider(balancerService, balancerPoolingHistoryProvider) {

    override fun getNetwork(): Network {
        return Network.POLYGON_ZKEVM
    }
}