package io.defitrack.protocol.balancer.staking

import io.defitrack.conditional.ConditionalOnCompany
import io.defitrack.protocol.Company
import io.defitrack.protocol.balancer.pooling.v3.BalancerEthereumPoolingMarketProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@ConditionalOnCompany(Company.BALANCER)
@Component
@ConditionalOnProperty(value = ["ethereum.enabled"], havingValue = "true", matchIfMissing = true)
class BalancerEthereumGaugeMarketProvider(
    balancerEthereumPoolingMarketProvider: BalancerEthereumPoolingMarketProvider
) : BalancerGaugeFarmingMarketProvider(
    balancerEthereumPoolingMarketProvider, "0x4e7bbd911cf1efa442bc1b2e9ea01ffe785412ec"
)