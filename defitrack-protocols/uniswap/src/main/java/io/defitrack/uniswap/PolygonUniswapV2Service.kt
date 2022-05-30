package io.defitrack.uniswap

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class PolygonUniswapV2Service(
    objectMapper: ObjectMapper,
    graphGatewayProvider: TheGraphGatewayProvider
) : AbstractUniswapV2Service(objectMapper, graphGatewayProvider) {

    override fun getGraphUrl(): String {
        return "https://api.thegraph.com/subgraphs/name/ianlapham/uniswap-v3-polygon"
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}