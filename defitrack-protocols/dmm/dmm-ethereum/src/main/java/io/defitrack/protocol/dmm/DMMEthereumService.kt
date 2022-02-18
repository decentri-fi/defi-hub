package io.defitrack.protocol.dmm

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class DMMEthereumService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {


    fun getPairDayData(pairId: String): List<DMMPairDayData> = runBlocking {
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
            object : TypeReference<List<DMMPairDayData>>() {

            })
    }


    fun getUserPoolings(user: String): List<DMMUser> = runBlocking {
        val query = """
            { 
                users(where: {id: "${user.lowercase()}"}) {
                  id
                liquidityPositions {
                  id
                  liquidityTokenBalance
                  pool {
                    id
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
            object : TypeReference<List<DMMUser>>() {

            })
    }

    fun getPoolingMarkets(): List<DMMPool> = runBlocking {
        val query = """
            {
            	pools(first: 500, where: { reserveUSD_gt: 1000 }) {
                id
                pair {
                    id
                }
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
            JsonParser.parseString(response).asJsonObject["data"].asJsonObject["pools"].toString()
        return@runBlocking objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<DMMPool>>() {

            })
    }


    fun query(query: String): String = runBlocking {
        client.request("https://api.thegraph.com/subgraphs/name/dynamic-amm/dynamic-amm") {
            method = HttpMethod.Post
            body = objectMapper.writeValueAsString(mapOf("query" to query))
        }
    }
}