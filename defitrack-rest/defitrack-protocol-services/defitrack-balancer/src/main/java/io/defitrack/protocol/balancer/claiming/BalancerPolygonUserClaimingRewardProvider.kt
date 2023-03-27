package io.defitrack.protocol.balancer.claiming

import io.defitrack.common.network.Network
import io.defitrack.protocol.balancer.staking.BalancerPolygonGaugeMarketProvider
import org.springframework.stereotype.Service

@Service
class BalancerPolygonUserClaimingRewardProvider(
    balancerPolygonStakingMarketService: BalancerPolygonGaugeMarketProvider,
) : BalancerClaimableRewardProvider(balancerPolygonStakingMarketService) {

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}
