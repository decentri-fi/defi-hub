package io.defitrack.protocol.balancer.claiming

import io.defitrack.common.network.Network
import io.defitrack.protocol.balancer.staking.BalancerArbitrumGaugeMarketProvider
import org.springframework.stereotype.Service

@Service
class BalancerArbitrumUserClaimingRewardProvider(
    gaugeMarketProvider: BalancerArbitrumGaugeMarketProvider,
) : BalancerClaimableRewardProvider(gaugeMarketProvider) {

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}
