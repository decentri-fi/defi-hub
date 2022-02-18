package io.defitrack.protocol

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import io.defitrack.protocol.sushi.domain.PairDayData
import io.defitrack.protocol.sushi.domain.SushiUser
import io.defitrack.protocol.sushi.domain.SushiswapPair
import io.defitrack.thegraph.TheGraphGateway
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.*

class SpiritGraphGateway(
    private val graphGateway: TheGraphGateway,
    private val objectMapper: ObjectMapper
) {

    fun getPairs(): List<SushiswapPair> = runBlocking(Dispatchers.IO) {
        val query = """
        {
            pairs(first: 100, orderDirection: desc, orderBy: volumeUSD) {
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

        val poolSharesAsString =
            graphGateway.performQuery(query).asJsonObject["pairs"].toString()
        return@runBlocking objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<SushiswapPair>>() {

            })
    }

    fun getPairDayData(pairId: String): List<PairDayData> = runBlocking {
        val query = """
           {
                pairDayDatas(first: 8, orderBy: date, orderDirection: desc where: {pairAddress: "$pairId"}) {
                id,
                dailyVolumeUSD
              }
            }
        """.trimIndent()

        val poolSharesAsString =
           graphGateway.performQuery(query).asJsonObject["pairDayDatas"].toString()
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

            val poolSharesAsString =
                graphGateway.performQuery(query).asJsonObject["users"].toString()
            return@runBlocking objectMapper.readValue(poolSharesAsString,
                object : TypeReference<List<SushiUser>>() {

                })
        }
    }
}