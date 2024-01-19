package io.defitrack.uniswap.v2

import io.defitrack.common.network.Network
import io.defitrack.protocol.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class EthereumUniswapV2Service(
    graphGatewayProvider: TheGraphGatewayProvider
) : AbstractUniswapV2Service("https://api.thegraph.com/subgraphs/name/uniswap/uniswap-v2", graphGatewayProvider) {

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}