package io.defitrack.uniswap.v3

import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class UniswapV3Service(
    graphGatewayProvider: TheGraphGatewayProvider
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/uniswap/uniswap-v3",
    graphGatewayProvider
) {

    suspend fun getPairDayData(pairId: String): List<PoolDayData> {
        val query = """
              {
                poolDayDatas(first: 8, orderBy: date, orderDirection: desc where: {id: "$pairId"}) {
                id,
                volumeUSD
              }
            }
        """.trimIndent()
        return query(query, "poolDayDatas")

    }
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