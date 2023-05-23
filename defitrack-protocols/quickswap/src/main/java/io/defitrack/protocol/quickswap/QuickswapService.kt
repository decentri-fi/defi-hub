package io.defitrack.protocol.quickswap

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.protocol.quickswap.domain.PairDayData
import io.defitrack.protocol.quickswap.domain.QuickswapPair
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.days


@Component
class QuickswapService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient,
    graphGatewayProvider: TheGraphGatewayProvider
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/sameepsi/quickswap06", graphGatewayProvider
) {

    val vaultCache = Cache.Builder<String, List<String>>().expireAfterWrite(
        1.days
    ).build()

    val pairCache = Cache.Builder<String, List<QuickswapPair>>().expireAfterWrite(
        1.days
    ).build()

    fun getOldDQuickContract(): String {
        return "0xf28164a485b0b2c90639e47b0f377b4a438a16b1"
    }

    fun getDQuickContract(): String {
        return "0x958d208cdf087843e9ad98d23823d32e17d723a1"
    }

    fun getDualRewardFactory(): String {
        return "0x9dd12421c637689c3fc6e661c9e2f02c2f61b3eb"
    }

    fun getRewardFactory(): String {
        return "0x8aaa5e259f74c8114e0a471d9f2adfc66bfe09ed"
    }

    fun getOldRewardFactory(): String {
        return "0x8aaa5e259f74c8114e0a471d9f2adfc66bfe09ed"
    }

    fun getDeprecatedRewardFactory(): String {
        return "0x5eec262b05a57da9beb5fe96a34aa4ed0c5e029f"
    }


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

    suspend fun getPairs(): List<QuickswapPair> {
        return pairCache.get("all-pairs") {
            val query = """
   {
            	pairs(first: 1000, orderDirection: desc, orderBy: reserveUSD, where: {reserveUSD_gt: 10000}) {
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
}