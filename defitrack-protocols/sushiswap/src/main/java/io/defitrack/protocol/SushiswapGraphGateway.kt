package io.defitrack.protocol

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.protocol.sushi.domain.PairDayData
import io.defitrack.protocol.sushi.domain.SushiUser
import io.defitrack.protocol.sushi.domain.SushiswapPair
import io.defitrack.thegraph.TheGraphGatewayProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.*

class SushiswapGraphGateway(
    private val objectMapper: ObjectMapper,
    endpoint: String,
    graphGatewayProvider: TheGraphGatewayProvider,
) {

    val graph = graphGatewayProvider.createTheGraphGateway(endpoint)

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

        val poolSharesAsString = graph.performQuery(query).asJsonObject["pairs"].toString()
        return@runBlocking objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<SushiswapPair>>() {

            })
    }

    fun getPairDayData(pairId: String): List<PairDayData> = runBlocking {
        val query = """
           {
                pairDayDatas(first: 8, orderBy: date, orderDirection: desc where: {pair: "$pairId"}) {
                id,
                volumeUSD
              }
            }
        """.trimIndent()

        val data = graph.performQuery(query).asJsonObject["pairDayDatas"].toString()
        return@runBlocking objectMapper.readValue(data,
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
            val data = graph.performQuery(query).asJsonObject["users"].toString()
            return@runBlocking objectMapper.readValue(data,
                object : TypeReference<List<SushiUser>>() {

                })
        }
    }
}