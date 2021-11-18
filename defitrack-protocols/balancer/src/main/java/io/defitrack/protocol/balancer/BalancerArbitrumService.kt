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
class BalancerArbitrumService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    fun getPool(poolAddress: String): Pool? = runBlocking {
        val query = """
            {
              pools(where: {address: "$poolAddress"}) {
                address
                id
                totalLiquidity,
                totalShares
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

    fun query(query: String): String = runBlocking {
        client.request("https://api.thegraph.com/subgraphs/name/balancer-labs/balancer-arbitrum-v2") {
            method = HttpMethod.Post
            body = objectMapper.writeValueAsString(mapOf("query" to query))
        }
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