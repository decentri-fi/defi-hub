package io.defitrack.uniswap

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.common.network.Network
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class EthereumUniswapV2Service(
    objectMapper: ObjectMapper,
    graphGatewayProvider: TheGraphGatewayProvider
) : AbstractUniswapV2Service(objectMapper, graphGatewayProvider) {

    override fun getGraphUrl(): String {
        return "https://api.thegraph.com/subgraphs/name/uniswap/uniswap-v2"
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}