package io.defitrack.protocol.dmm

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import io.defitrack.thegraph.TheGraphGatewayProvider
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class DMMPolygonService(
    private val objectMapper: ObjectMapper,
    graphGatewayProvider: TheGraphGatewayProvider,
) {
    val graph = graphGatewayProvider.createTheGraphGateway("https://api.thegraph.com/subgraphs/name/ducquangkstn/dmm-subgraph-matic")

    fun getPairDayData(pairId: String): List<DMMPairDayData> = runBlocking {
        val query = """
           {
                pairDayDatas(first: 8, orderBy: date, orderDirection: desc where: {pairAddress: "$pairId"}) {
                id,
                dailyVolumeUSD
              }
            }
        """.trimIndent()

        val poolSharesAsString = graph.performQuery(query).asJsonObject["pairDayDatas"].toString()
        return@runBlocking objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<DMMPairDayData>>() {

            })
    }


    fun getPoolingMarkets(): List<DMMPool> = runBlocking {
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
        val poolSharesAsString = graph.performQuery(query).asJsonObject["pools"].toString()
        return@runBlocking objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<DMMPool>>() {

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
        val poolSharesAsString = graph.performQuery(query).asJsonObject["users"].toString()
        return@runBlocking objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<DMMUser>>() {

            })
    }
}