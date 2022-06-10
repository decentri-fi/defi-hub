package io.defitrack.protocol.kyberswap

import io.defitrack.protocol.kyberswap.domain.PairDayData
import io.defitrack.protocol.kyberswap.domain.Pool
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Service

@Service
class KyberswapPolygonGraphProvider(
    graphGatewayProvider: TheGraphGatewayProvider,
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/ducquangkstn/dmm-subgraph-matic",
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

        return query(query, "pairDayDatas")
    }


    suspend fun getPoolingMarkets(): List<Pool> {
        val query = """
            {
            	pools(first: 500, where: { reserveUSD_gt: 1000 }) {
                id
                reserveUSD
                pair {
                    id
                }
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