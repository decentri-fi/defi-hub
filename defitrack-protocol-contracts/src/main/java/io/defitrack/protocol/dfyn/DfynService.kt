package io.defitrack.protocol.dfyn

import io.defitrack.protocol.dfyn.domain.Pair
import io.defitrack.protocol.dfyn.domain.PairDayData
import io.defitrack.protocol.thegraph.GraphProvider
import io.defitrack.protocol.thegraph.TheGraphGatewayProvider
import io.github.reactivecircus.cache4k.Cache
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.days

@Component
@Deprecated("don't use graph anymore")
class DfynService(
    graphGatewayProvider: TheGraphGatewayProvider
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/ss-sonic/dfyn-v5", graphGatewayProvider
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

    suspend fun getPairs(): List<Pair> {
        val query = """
            {
            	pairs(first: 200, orderDirection: desc, orderBy: volumeUSD) {
                id
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

        return query(query, "pairs")
    }
}