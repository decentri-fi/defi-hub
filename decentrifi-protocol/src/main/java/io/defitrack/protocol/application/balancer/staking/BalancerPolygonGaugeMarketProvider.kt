package io.defitrack.protocol.application.balancer.staking

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.application.balancer.pooling.v3.BalancerPolygonPoolingMarketProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BALANCER)
@ConditionalOnProperty(value = ["polygon.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerPolygonGaugeMarketProvider(
    balancerPoolingMarketProvider: BalancerPolygonPoolingMarketProvider
) : BalancerGaugeFarmingMarketProvider(
    balancerPoolingMarketProvider, "0x3b8ca519122cdd8efb272b0d3085453404b25bd0"
)