package io.defitrack.protocol

import io.defitrack.common.network.Network
import org.springframework.stereotype.Component

@Component
class HopOptimismService : AbstractHopService {

    override fun getStakingRewards(): List<String> {
        return listOf(
            "0x09992dd7b32f7b35d347de9bdaf1919a57d38e82", //snx v2
            "0xf587b9309c603feedf0445af4d3b21300989e93a", //usdc
            "0x392b9780cfd362bd6951edfa9ebc31e68748b190", //dai
            "0xaeb1b49921e0d2d96fcdbe0d486190b2907b3e0b", //usdt,
            "0x95d6a95becfd98a7032ed0c7d950ff6e0fa8d697", //eth
        )
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }

    override fun getGraph(): String {
        return "https://api.thegraph.com/subgraphs/name/hop-protocol/hop-polygon"
    }
}