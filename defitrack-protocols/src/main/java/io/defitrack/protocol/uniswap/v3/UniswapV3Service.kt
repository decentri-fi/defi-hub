package io.defitrack.uniswap.v3

import io.defitrack.protocol.thegraph.GraphProvider
import io.defitrack.protocol.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class UniswapV3Service(
    graphGatewayProvider: TheGraphGatewayProvider
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/uniswap/uniswap-v3",
    graphGatewayProvider
) {

    suspend fun providePools(): List<UniswapV3Pool> {
        val query = """
            {
                pools(where: {totalValueLockedUSD_gt: 10000}) {
                  id
                  token0 {
                      id,
                      symbol
                      name
                      decimals
                  }
                    token1 {
                      id,
                      symbol
                      name
                      decimals
                    }                     
                }
            }
        """.trimIndent()
        return query(query, "pools")
    }
}