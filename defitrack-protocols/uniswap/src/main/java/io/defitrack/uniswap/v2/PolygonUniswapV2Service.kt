package io.defitrack.uniswap.v2

import io.defitrack.common.network.Network
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class PolygonUniswapV2Service(
    graphGatewayProvider: TheGraphGatewayProvider
) : AbstractUniswapV2Service(
    "https://api.thegraph.com/subgraphs/name/ianlapham/uniswap-v3-polygon",
    graphGatewayProvider
) {

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}