package io.defitrack.protocol.application.balancer.pooling.v3

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.balancer.contract.BalancerService
import io.defitrack.protocol.application.balancer.pooling.history.BalancerPoolingHistoryProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BALANCER)
@ConditionalOnProperty(value = ["arbitrum.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerArbitrumPoolingMarketProvider(
    balancerService: BalancerService, balancerPoolingHistoryProvider: BalancerPoolingHistoryProvider
) : BalancerPoolingMarketProvider(
    balancerService, balancerPoolingHistoryProvider
) {

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}