package io.defitrack.protocol.balancer.claiming

import io.defitrack.common.network.Network
import io.defitrack.protocol.balancer.staking.BalancerOptimismGaugeMarketProvider
import io.defitrack.protocol.balancer.staking.BalancerPolygonGaugeMarketProvider
import org.springframework.stereotype.Service

@Service
class BalancerOptimismUserClaimingRewardProvider(
    gaugeMarketProvider: BalancerOptimismGaugeMarketProvider,
) : BalancerClaimableRewardProvider(gaugeMarketProvider) {

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }
}
