package io.defitrack.protocol.application.balancer.staking

import io.defitrack.architecture.conditional.ConditionalOnCompany
import io.defitrack.architecture.conditional.ConditionalOnNetwork
import io.defitrack.common.network.Network
import io.defitrack.protocol.Company
import io.defitrack.protocol.application.balancer.pooling.v3.BalancerOptimismPoolingMarketProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnCompany(Company.BALANCER)
@ConditionalOnNetwork(Network.OPTIMISM)
class BalancerOptimismGaugeMarketProvider(
    poolingMarketProvider: BalancerOptimismPoolingMarketProvider
) : BalancerGaugeFarmingMarketProvider(
    poolingMarketProvider, "0x2E96068b3D5B5BAE3D7515da4A1D2E52d08A2647"
)