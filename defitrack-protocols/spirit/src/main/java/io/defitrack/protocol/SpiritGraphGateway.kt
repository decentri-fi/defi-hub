package io.defitrack.protocol

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import io.defitrack.protocol.sushi.domain.PairDayData
import io.defitrack.protocol.sushi.domain.SushiUser
import io.defitrack.protocol.sushi.domain.SushiswapPair
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.*

class SpiritGraphGateway(
    private val objectMapper: ObjectMapper,
    private val endpoint: String,
    private val client: HttpClient
) {

    fun getPairs(): List<SushiswapPair> = runBlocking(Dispatchers.IO) {
        val query = """
        {
            pairs(first: 500, orderDirection: desc, orderBy: volumeUSD) {
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
            object : TypeReference<List<SushiswapPair>>() {

            })
    }

    fun getPairDayData(pairId: String) = runBlocking {
        val query = """
           {
                pairDayDatas(first: 8, orderBy: date, orderDirection: desc where: {pair: "$pairId"}) {
                id,
                volumeUSD
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

    fun getUserPoolings(user: String): List<SushiUser> {
        return runBlocking {
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

            val response = query(query)
            val poolSharesAsString =
                JsonParser.parseString(response).asJsonObject["data"].asJsonObject["users"].toString()
            return@runBlocking objectMapper.readValue(poolSharesAsString,
                object : TypeReference<List<SushiUser>>() {

                })
        }
    }

    private suspend fun query(query: String): String {
        return client.request(endpoint) {
            method = HttpMethod.Post
            body = objectMapper.writeValueAsString(mapOf("query" to query))
        }
    }
}