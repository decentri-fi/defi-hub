package io.defitrack.protocol.application.balancer.pooling.v3

import io.defitrack.common.network.Network
import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.balancer.contract.BalancerService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BALANCER)
@ConditionalOnProperty(value = ["ethereum.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerEthereumPoolingMarketProvider(
    balancerService: BalancerService,
) : BalancerPoolingMarketProvider(
    balancerService
) {

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}