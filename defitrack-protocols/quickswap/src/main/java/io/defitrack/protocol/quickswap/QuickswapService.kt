package io.defitrack.protocol.quickswap

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.protocol.quickswap.domain.PairDayData
import io.defitrack.protocol.quickswap.domain.QuickLpPools
import io.defitrack.protocol.quickswap.domain.QuickswapPair
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
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

    val vaultCache = Cache.Builder().expireAfterWrite(
        1.days
    ).build<String, List<String>>()

    val pairCache = Cache.Builder().expireAfterWrite(
        1.days
    ).build<String, List<QuickswapPair>>()

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

    fun getDeprecatedRewardFactory(): String {
        return "0x8aaa5e259f74c8114e0a471d9f2adfc66bfe09ed"
    }

    suspend fun getVaultAddresses(): List<String> {
        return vaultCache.get("quickswap-normal-vaults") {
            val maticVaultsEndpoint =
                "https://raw.githubusercontent.com/beefyfinance/beefy-api/master/src/data/matic/quickLpPools.json"
            val result: String = client.get(maticVaultsEndpoint).bodyAsText()

            objectMapper.readValue(
                result,
                object : TypeReference<List<QuickLpPools>>() {
                }).map {
                it.rewardPool
            }
        }
    }

    fun getDualVaultAddresses(): List<String> {
        return runBlocking {
            vaultCache.get("quickswap-dual-vaults") {
                val maticVaultsEndpoint =
                    "https://raw.githubusercontent.com/beefyfinance/beefy-api/master/src/data/matic/quickDualLpPools.json"
                client.get(maticVaultsEndpoint).body()
            }
        }
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
            	pairs(first: 500, orderDirection: desc, orderBy: reserveUSD, where: {reserveUSD_gt: 10000}) {
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