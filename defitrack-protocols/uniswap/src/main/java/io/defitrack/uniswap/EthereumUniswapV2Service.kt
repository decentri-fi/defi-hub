package io.defitrack.uniswap

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.ktor.client.*
import org.springframework.stereotype.Component

@Component
class EthereumUniswapV2Service(
    objectMapper: ObjectMapper,
    httpClient: HttpClient
) : AbstractUniswapV2Service(objectMapper, httpClient) {

    override fun getGraphUrl(): String {
        return "https://api.thegraph.com/subgraphs/name/uniswap/uniswap-v2"
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}