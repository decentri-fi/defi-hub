package io.defitrack.protocol.kyberswap.graph

import io.defitrack.protocol.kyberswap.graph.domain.PairDayData
import io.defitrack.protocol.kyberswap.graph.domain.Pool
import io.defitrack.protocol.thegraph.GraphProvider
import io.defitrack.protocol.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Service

@Service
class KyberswapEthereumGraphProvider(
    graphGatewayProvider: TheGraphGatewayProvider,
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/dynamic-amm/dynamic-amm",
    graphGatewayProvider
) {

    suspend fun getPairDayData(pairId: String): List<PairDayData> {
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

    suspend fun getPoolingMarkets(): List<Pool> {
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