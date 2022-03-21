package io.defitrack.uniswap

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import io.defitrack.common.network.Network
import io.defitrack.uniswap.domain.PairDayData
import io.defitrack.uniswap.domain.UniswapPair
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.days

@Component
abstract class AbstractUniswapV2Service(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    fun getPairDayData(pairId: String): List<PairDayData> = runBlocking {
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

    private val pairCache =
        Cache.Builder().expireAfterWrite(1.days).build<String, List<UniswapPair>>()

    fun getPairs(): List<UniswapPair> = runBlocking {
        pairCache.get("all") {
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
                object : TypeReference<List<UniswapPair>>() {

                })
        }
    }

    fun query(query: String): String = runBlocking {
        client.request(getGraphUrl()) {
            method = HttpMethod.Post
            body = objectMapper.writeValueAsString(mapOf("query" to query))
        }
    }

    abstract fun getGraphUrl(): String
    abstract fun getNetwork(): Network
}