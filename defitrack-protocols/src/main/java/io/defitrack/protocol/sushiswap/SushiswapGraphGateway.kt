package io.defitrack.protocol.sushiswap

import io.defitrack.protocol.sushiswap.domain.PairDayData
import io.defitrack.protocol.sushiswap.domain.SushiUser
import io.defitrack.protocol.sushiswap.domain.SushiswapPair
import io.defitrack.protocol.thegraph.GraphProvider
import io.defitrack.protocol.thegraph.TheGraphGatewayProvider
import java.util.*

class SushiswapGraphGateway(
    url: String,
    graphGatewayProvider: TheGraphGatewayProvider,
) : GraphProvider(url, graphGatewayProvider) {

    suspend fun getPairs(): List<SushiswapPair> {
        val query = """
        {
            pairs(first: 500, orderDirection: desc, orderBy: volumeUSD, where: {reserveUSD_gt: 25000}) {
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

    suspend fun getPairDayData(pairId: String): List<PairDayData> {
        val query = """
           {
                pairDayDatas(first: 8, orderBy: date, orderDirection: desc where: {pair: "$pairId"}) {
                id,
                volumeUSD
              }
            }
        """.trimIndent()

        return query(query, "pairDayDatas")
    }

    suspend fun getUserPoolings(user: String): List<SushiUser> {
        val query = """
            { 
                users(where: {id: "${user.lowercase(Locale.getDefault())}"}) {
                  id
                liquidityPositions {
                  id
                  liquidityTokenBalance
                  pair {
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
                }
            }
        """.trimIndent()
        return query(query, "users")
    }
}