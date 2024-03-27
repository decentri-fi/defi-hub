package io.defitrack.protocol.application.balancer.staking

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.common.network.Network
import io.defitrack.protocol.Company
import io.defitrack.protocol.application.balancer.pooling.v3.BalancerArbitrumPoolingMarketProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BALANCER)
@ConditionalOnNetwork(Network.ARBITRUM)
class BalancerArbitrumGaugeMarketProvider(
    poolingMarketProvider: BalancerArbitrumPoolingMarketProvider
) : BalancerGaugeFarmingMarketProvider(
    poolingMarketProvider, "0xb08E16cFc07C684dAA2f93C70323BAdb2A6CBFd2"
)