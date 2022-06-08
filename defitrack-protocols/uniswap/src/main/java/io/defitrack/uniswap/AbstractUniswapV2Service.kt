package io.defitrack.uniswap

import io.defitrack.common.network.Network
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import io.defitrack.uniswap.domain.PairDayData
import io.defitrack.uniswap.domain.UniswapPair
import io.github.reactivecircus.cache4k.Cache
import kotlin.time.Duration.Companion.days

abstract class AbstractUniswapV2Service(
    url: String,
    graphGatewayProvider: TheGraphGatewayProvider
) : GraphProvider(url, graphGatewayProvider) {

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

    private val pairCache =
        Cache.Builder().expireAfterWrite(1.days).build<String, List<UniswapPair>>()

    suspend fun getPairs(): List<UniswapPair> {
        return pairCache.get("all") {
            val query = """
            {
            	pairs(first: 500, orderDirection: desc, orderBy: reserveUSD) {
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

            query(query, "pairs")
        }
    }
    abstract fun getNetwork(): Network
}