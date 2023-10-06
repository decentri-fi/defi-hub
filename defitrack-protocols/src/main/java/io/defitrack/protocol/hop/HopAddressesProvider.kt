package io.defitrack.protocol.hop

import io.defitrack.common.network.Network
import org.springframework.stereotype.Component

@Component
class HopAddressesProvider {

    val config = mapOf(
        Network.BASE to HopConfig(
            graph = "https://api.thegraph.com/subgraphs/name/hop-protocol/hop-base",
        ),
        Network.ARBITRUM to HopConfig(
            graph = "https://api.thegraph.com/subgraphs/name/hop-protocol/hop-arbitrum"
        ),
        Network.OPTIMISM to HopConfig(
            graph = "https://api.thegraph.com/subgraphs/name/hop-protocol/hop-polygon"
        ),
        Network.POLYGON to HopConfig(
            graph = "https://api.thegraph.com/subgraphs/name/hop-protocol/hop-polygon"
        )
    )


    data class HopConfig(
        val graph: String,
    )
}