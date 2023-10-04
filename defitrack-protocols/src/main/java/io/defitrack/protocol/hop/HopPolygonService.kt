package io.defitrack.protocol.hop

import io.defitrack.common.network.Network
import org.springframework.stereotype.Component

@Component
class HopPolygonService : AbstractHopService {

    override fun getStakingRewards(): List<String> {
        return listOf(
            "0x7bceda1db99d64f25efa279bb11ce48e15fda427",
            "0x7dEEbCaD1416110022F444B03aEb1D20eB4Ea53f",
            "0x4Aeb0B5B1F3e74314A7Fa934dB090af603E8289b",
            "0x07932e9a5ab8800922b2688fb1fa0daad8341772",
            "0x2c2ab81cf235e86374468b387e241df22459a265",
            "0xaa7b3a4a084e6461d486e53a03cf45004f0963b7", //eth v2
            "0x7811737716942967ae6567b26a5051cc72af550e", //usdc v2
            "0xd6dc6f69f81537fe9decc18152b7005b45dc2ee7", //dai v2,
            "0x297e5079df8173ae1696899d3eacd708f0af82ce", //usdt v2
        )
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }

    override fun getGraph(): String {
        return "https://api.thegraph.com/subgraphs/name/hop-protocol/hop-polygon"
    }
}