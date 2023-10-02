package io.defitrack.protocol.compound.rewards

import io.defitrack.common.network.Network
import org.springframework.stereotype.Component

@Component
class CompoundArbitrumRewardProvider(
) : CompoundRewardProvider() {

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }
}