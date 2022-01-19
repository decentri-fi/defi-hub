package io.defitrack.uniswap

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.ktor.client.*
import org.springframework.stereotype.Component

@Component
class PolygonUniswapService(
    objectMapper: ObjectMapper,
    httpClient: HttpClient
) : AbstractUniswapService(objectMapper, httpClient) {

    override fun getGraphUrl(): String {
        return "https://api.thegraph.com/subgraphs/name/ianlapham/uniswap-v3-polygon"
    }

    override fun getNetwork(): Network {
        return Network.POLYGON
    }
}