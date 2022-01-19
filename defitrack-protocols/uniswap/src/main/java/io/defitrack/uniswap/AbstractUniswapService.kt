package io.defitrack.uniswap

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import io.defitrack.common.network.Network
import io.defitrack.uniswap.domain.PairDayData
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import io.defitrack.uniswap.domain.*

@Component
abstract class AbstractUniswapService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

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

    @Cacheable(cacheNames = ["uniswap-pairs"], key = "'all'")
    fun getPairs(): List<UniswapPair> = runBlocking {
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
        return@runBlocking objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<UniswapPair>>() {

            })
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