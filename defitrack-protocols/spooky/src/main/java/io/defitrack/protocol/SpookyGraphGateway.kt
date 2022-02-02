package io.defitrack.protocol

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.protocol.sushi.domain.PairDayData
import io.defitrack.protocol.sushi.domain.SushiUser
import io.defitrack.protocol.sushi.domain.SushiswapPair
import io.defitrack.thegraph.TheGraphGateway
import java.util.*

class SpookyGraphGateway(
    private val objectMapper: ObjectMapper,
    private val theGraphGateway: TheGraphGateway
) {

    suspend fun getPairs(): List<SushiswapPair> {
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

        val poolSharesAsString = theGraphGateway.performQuery(query).asJsonObject["pairs"].toString()

        return objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<SushiswapPair>>() {

            })
    }

    suspend fun getPairDayData(pairId: String): List<PairDayData> {
        val query = """
           {
                pairDayDatas(first: 8, orderBy: date, orderDirection: desc where: {pair: "$pairId"}) {
                id,
                volumeUSD
              }
            }
        """.trimIndent()

        val poolSharesAsString = theGraphGateway.performQuery(query).asJsonObject["pairDayDatas"].toString()
        return objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<PairDayData>>() {

            })
    }

    suspend fun getUserPoolings(user: String): List<SushiUser> {
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

        val usersAsString = theGraphGateway.performQuery(query).asJsonObject["users"].toString()
        return objectMapper.readValue(usersAsString,
            object : TypeReference<List<SushiUser>>() {

            })
    }
}