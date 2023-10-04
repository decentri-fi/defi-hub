package io.defitrack.uniswap.v2

import io.defitrack.common.network.Network
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import io.defitrack.uniswap.v2.domain.PairDayData
import io.defitrack.uniswap.v2.domain.UniswapPair
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
        Cache.Builder<String, List<UniswapPair>>().expireAfterWrite(1.days).build()

    suspend fun getPairs(): List<UniswapPair> {
        return pairCache.get("all") {
            val query = """
            {
            	pairs(orderDirection: desc, orderBy: reserveUSD, where: {reserveUSD_gt: 10000}, first: 1000) {
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