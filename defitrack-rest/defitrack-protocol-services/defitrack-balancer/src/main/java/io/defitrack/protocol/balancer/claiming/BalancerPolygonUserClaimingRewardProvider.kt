package io.defitrack.protocol.balancer.claiming

import io.defitrack.common.network.Network
import io.defitrack.protocol.balancer.staking.BalancerPolygonFarmingMarketProvider
import org.springframework.stereotype.Service

@Service
class BalancerPolygonUserClaimingRewardProvider(
    balancerPolygonStakingMarketService: BalancerPolygonFarmingMarketProvider,
) : BalancerClaimableRewardProvider(balancerPolygonStakingMarketService) {

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}
