package io.defitrack.protocol.balancer.staking

import io.defitrack.common.network.Network
import io.defitrack.protocol.balancer.graph.BalancerGaugeOptimismGraphProvider
import org.springframework.stereotype.Component

@Component
class BalancerOptimismFarmingMarketProvider(
    gaugeProvider: BalancerGaugeOptimismGraphProvider
) : BalancerGaugeFarmingMarketProvider(gaugeProvider) {

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}