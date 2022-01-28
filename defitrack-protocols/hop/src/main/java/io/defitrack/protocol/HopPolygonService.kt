package io.defitrack.protocol

import io.defitrack.common.network.Network
import org.springframework.stereotype.Component

@Component
class HopPolygonService : AbstractHopService {

    override fun getStakingRewards(): List<String> {
        return listOf("0x7bceda1db99d64f25efa279bb11ce48e15fda427")
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }

    override fun getGraph(): String {
        return "https://api.thegraph.com/subgraphs/name/hop-protocol/hop-polygon"
    }
}