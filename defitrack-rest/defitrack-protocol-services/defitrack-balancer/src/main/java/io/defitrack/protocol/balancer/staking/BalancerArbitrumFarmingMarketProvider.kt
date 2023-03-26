package io.defitrack.protocol.balancer.staking

import io.defitrack.common.network.Network
import io.defitrack.protocol.balancer.graph.BalancerGaugeArbitrumGraphProvider
import org.springframework.stereotype.Component

@Component
class BalancerArbitrumFarmingMarketProvider(
    gaugeProvider: BalancerGaugeArbitrumGraphProvider,
) : BalancerGaugeFarmingMarketProvider(gaugeProvider) {

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}