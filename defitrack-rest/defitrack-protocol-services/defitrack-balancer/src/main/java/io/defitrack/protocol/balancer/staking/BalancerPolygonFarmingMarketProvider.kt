package io.defitrack.protocol.balancer.staking

import io.defitrack.common.network.Network
import io.defitrack.protocol.balancer.graph.BalancerGaugePolygonGraphProvider
import org.springframework.stereotype.Component

@Component
class BalancerPolygonFarmingMarketProvider(
    gaugeProvider: BalancerGaugePolygonGraphProvider,
) :
    BalancerGaugeFarmingMarketProvider(gaugeProvider) {

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}