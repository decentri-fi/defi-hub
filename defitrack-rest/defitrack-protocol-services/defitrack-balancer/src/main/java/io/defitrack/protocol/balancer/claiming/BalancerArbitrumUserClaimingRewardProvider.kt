package io.defitrack.protocol.balancer.claiming

import io.defitrack.common.network.Network
import io.defitrack.protocol.balancer.staking.BalancerArbitrumFarmingMarketProvider
import org.springframework.stereotype.Service

@Service
class BalancerArbitrumUserClaimingRewardProvider(
    marketProvider: BalancerArbitrumFarmingMarketProvider,
) : BalancerClaimableRewardProvider(marketProvider) {

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}
