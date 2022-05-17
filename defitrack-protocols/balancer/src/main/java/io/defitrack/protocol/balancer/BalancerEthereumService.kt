package io.defitrack.protocol.balancer

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.util.*

@Component
class BalancerEthereumService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    fun query(query: String): String = runBlocking {
        client.request("https://thegraph.com/hosted-service/subgraph/messari/balancer-v2-ethereum") {
            method = HttpMethod.Post
            body = objectMapper.writeValueAsString(mapOf("query" to query))
        }
    }

    fun getPool(poolAddress: String): Pool? = runBlocking {
        val query = """
            {
              liquidityPools(where: {id: "$poolAddress"}){
                id
                name
                symbol
                totalValueLockedUSD
                cumulativeVolumeUSD
                inputTokens{
                  id
                  symbol
                  name
                  decimals
                }
                inputTokenBalances
                outputToken{
                  id
                  symbol
                  name
                  decimals
                }
                outputTokenSupply
                
              }
            }
        """.trimIndent()

        val response = query(query)
        val poolSharesAsString =
            JsonParser.parseString(response).asJsonObject["data"].asJsonObject["pools"].toString()
        return@runBlocking objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<Pool>>() {

            }).firstOrNull()
    }

    fun getBalances(address: String): List<PoolShare> = runBlocking {
        val query = """
            {
              poolShares(where: {userAddress: "${address.lowercase(Locale.getDefault())}"}) {
                userAddress {
                  id
                },
                poolId {
                  id
                  totalShares
                  totalLiquidity
                  address
                  tokens {
                  	id
                    address
                    symbol
                    name
                    decimals
                    balance
                  }
                  symbol
                  name
                },
                balance
              }
           }
        """.trimIndent()

        val response =
            query(query)
        val poolSharesAsString =
            JsonParser.parseString(response).asJsonObject["data"].asJsonObject["poolShares"].toString()
        return@runBlocking objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<PoolShare>>() {

            })
    }
}