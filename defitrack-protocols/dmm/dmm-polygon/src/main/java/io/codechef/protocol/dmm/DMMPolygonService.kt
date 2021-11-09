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
class DMMPolygonService(
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
            object : TypeReference<List<DMMPairDayData>>() {

            })
    }


    fun getPoolingMarkets() = runBlocking {
        val query = """
            {
            	pools(first: 500, where: { reserveUSD_gt: 1000 }) {
                id
                reserveUSD
                pair {
                    id
                }
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

    fun getUserPoolings(user: String) = runBlocking {
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

    fun query(query: String): String = runBlocking {
        client.request("https://api.thegraph.com/subgraphs/name/ducquangkstn/dmm-subgraph-matic\n") {
            method = HttpMethod.Post
            body = objectMapper.writeValueAsString(mapOf("query" to query))
        }
    }
}