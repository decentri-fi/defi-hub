package io.defitrack.protocol

import io.defitrack.common.network.Network
import org.springframework.stereotype.Component

@Component
class HopArbitrumService : AbstractHopService {

    override fun getStakingRewards(): List<String> {
        return listOf(
            "0xb0CabFE930642AD3E7DECdc741884d8C3F7EbC70",
            "0x9Dd8685463285aD5a94D2c128bda3c5e8a6173c8",
            "0xd4D28588ac1D9EF272aa29d4424e3E2A03789D1E",
            "0x755569159598f3702bdD7DFF6233A317C156d3Dd",
            "0x3D4cAD734B464Ed6EdCF6254C2A3e5fA5D449b32"
        )
    }

    override fun getNetwork(): Network {
        return Network.ARBITRUM
    }

    override fun getGraph(): String {
        return "https://api.thegraph.com/subgraphs/name/hop-protocol/hop-arbitrum"
    }
}