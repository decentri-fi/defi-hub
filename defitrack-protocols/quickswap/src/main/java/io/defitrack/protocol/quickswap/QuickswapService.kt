package io.defitrack.quickswap

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import io.defitrack.quickswap.dto.PairDayData
import io.defitrack.quickswap.dto.QuickLpPools
import io.defitrack.quickswap.dto.QuickswapPair
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


@Component
class QuickswapService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    @OptIn(ExperimentalTime::class)
    val vaultCache = Cache.Builder().expireAfterWrite(
        Duration.Companion.days(1)
    ).build<String, List<String>>()

    @OptIn(ExperimentalTime::class)
    val pairCache = Cache.Builder().expireAfterWrite(
        Duration.Companion.days(1)
    ).build<String, List<QuickswapPair>>()

    fun getDQuickContract(): String {
        return "0xf28164a485b0b2c90639e47b0f377b4a438a16b1"
    }

    fun getDualPools() = listOf(
        "0x3c1f53fed2238176419f8f897aec8791c499e3c8",
        "0x14977e7e263ff79c4c3159f497d9551fbe769625",
        "0xc0eb5d1316b835f4b584b59f922d9c87ca5053e5",
        "0xd26e16f5a9dfb9fe32db7f6386402b8aae1a5dd7"
    )

    fun getVaultAddresses(): List<String> {
        return runBlocking {
            vaultCache.get("quickswap-normal-vaults") {
                val maticVaultsEndpoint =
                    "https://raw.githubusercontent.com/beefyfinance/beefy-api/master/src/data/matic/quickLpPools.json"
                val result = client.get<String>(maticVaultsEndpoint)

                objectMapper.readValue(
                    result,
                    object : TypeReference<List<QuickLpPools>>() {
                    }).map {
                    it.rewardPool
                }
            }
        }
    }

    fun getDualVaultAddresses(): List<String> {
        return runBlocking {
            vaultCache.get("quickswap-dual-vaults") {
                val maticVaultsEndpoint =
                    "https://raw.githubusercontent.com/beefyfinance/beefy-api/master/src/data/matic/quickDualLpPools.json"
                val result = client.get<String>(maticVaultsEndpoint)

                objectMapper.readValue(
                    result,
                    object : TypeReference<List<QuickLpPools>>() {
                    }).map {
                    it.rewardPool
                }
            }
        }
    }

    fun getPairDayData(pairId: String) = runBlocking {
        val query = """
           {
                pairDayDatas(first: 8, orderBy: date, orderDirection: desc where: {pairAddress: "$pairId"}) {
                id,
                dailyVolumeUSD
              }
            }
        """.trimIndent()

        val response = query(query)
        val poolSharesAsString =
            JsonParser.parseString(response).asJsonObject["data"].asJsonObject["pairDayDatas"].toString()
        return@runBlocking objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<PairDayData>>() {

            })
    }

    fun getPairs(): List<QuickswapPair> = runBlocking {
        pairCache.get("all-pairs") {
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

            val response = query(query)
            val poolSharesAsString =
                JsonParser.parseString(response).asJsonObject["data"].asJsonObject["pairs"].toString()
            objectMapper.readValue(poolSharesAsString,
                object : TypeReference<List<QuickswapPair>>() {

                })
        }
    }

    fun query(query: String): String = runBlocking {
        client.request("https://api.thegraph.com/subgraphs/name/henrydapp/quickswap") {
            method = HttpMethod.Post
            body = objectMapper.writeValueAsString(mapOf("query" to query))
        }
    }
}