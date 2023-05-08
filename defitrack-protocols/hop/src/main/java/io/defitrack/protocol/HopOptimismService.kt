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
            "0x95d6a95becfd98a7032ed0c7d950ff6e0fa8d697", //eth,
            "0x266e2dc3C4c59E42AA07afeE5B09E964cFFe6778",
            "0x25FB92E505F752F730cAD0Bd4fa17ecE4A384266",
            "0x2935008eE9943f859C4fbb863c5402fFC06f462E",
            "0x25a5A48C35e75BD2EFf53D94f0BB60d5A00E36ea"
        )
    }

    override fun getNetwork(): Network {
        return Network.OPTIMISM
    }

    override fun getGraph(): String {
        return "https://api.thegraph.com/subgraphs/name/hop-protocol/hop-polygon"
    }
}