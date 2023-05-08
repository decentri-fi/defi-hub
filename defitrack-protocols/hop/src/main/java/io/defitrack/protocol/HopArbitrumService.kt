package io.defitrack.protocol

import io.defitrack.common.network.Network
import org.springframework.stereotype.Component

@Component
class HopArbitrumService : AbstractHopService {

    override fun getStakingRewards(): List<String> {
        return listOf(
        )
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }

    override fun getGraph(): String {
        return "https://api.thegraph.com/subgraphs/name/hop-protocol/hop-arbitrum"
    }
}