package io.defitrack.protocol.balancer.pooling

import io.defitrack.common.network.Network
import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.balancer.contract.BalancerService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BALANCER)
@ConditionalOnProperty(value = ["polygon.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerPolygonPoolingMarketProvider(balancerService: BalancerService) :
    BalancerPoolingMarketProvider(balancerService) {

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}