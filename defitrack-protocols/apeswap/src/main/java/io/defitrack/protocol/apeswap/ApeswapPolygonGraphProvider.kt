package io.defitrack.protocol.apeswap

import io.defitrack.messari.LiquidityPool
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class ApeswapPolygonGraphProvider(
    graphGatewayProvider: TheGraphGatewayProvider
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/messari/apeswap-polygon",
    graphGatewayProvider
) {

    suspend fun getPools(): List<LiquidityPool> {
        val query = """
            {
            	liquidityPools(first: 50, orderBy: totalValueLockedUSD, orderDirection: desc) {
                id
                name
                symbol,
                totalValueLockedUSD
                rewardTokens
              }
            }
        """.trimIndent()

        return query(query, "liquidityPools")
    }
}