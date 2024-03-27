package io.defitrack.protocol.application.balancer.staking

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.common.network.Network
import io.defitrack.protocol.Company
import io.defitrack.protocol.application.balancer.pooling.v3.BalancerEthereumPoolingMarketProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@ConditionalOnCompany(Company.BALANCER)
@Component
@ConditionalOnNetwork(Network.ETHEREUM)
class BalancerEthereumGaugeMarketProvider(
    balancerEthereumPoolingMarketProvider: BalancerEthereumPoolingMarketProvider
) : BalancerGaugeFarmingMarketProvider(
    balancerEthereumPoolingMarketProvider, "0x4e7bbd911cf1efa442bc1b2e9ea01ffe785412ec"
)