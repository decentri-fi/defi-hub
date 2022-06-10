package io.defitrack.protocol.dmm

import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Service

@Service
class KyberswapEthereumGraphProvider(
    graphGatewayProvider: TheGraphGatewayProvider,
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/dynamic-amm/dynamic-amm",
    graphGatewayProvider
) {

    suspend fun getPairDayData(pairId: String): List<DMMPairDayData> {
        val query = """
           {
                pairDayDatas(first: 8, orderBy: date, orderDirection: desc where: {pairAddress: "$pairId"}) {
                id,
                dailyVolumeUSD
              }
            }
        """.trimIndent()
        val data = graph.performQuery(query).asJsonObject["pairDayDatas"]
        return map(data)
    }

    suspend fun getPoolingMarkets(): List<DMMPool> {
        val query = """
            {
            	pools(first: 500, where: { reserveUSD_gt: 1000 }) {
                id
                pair {
                    id
                }
                reserveUSD
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