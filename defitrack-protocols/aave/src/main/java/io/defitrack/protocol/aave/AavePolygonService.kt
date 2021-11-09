package io.defitrack.protocol.aave

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import io.defitrack.aave.AaveReserve
import io.defitrack.aave.UserReserve
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class AavePolygonService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    fun getUserReserves(user: String) = runBlocking {
        val query = """
           { 
              userReserves(where: {user: "${user}"}) {
                reserve {
                  id
                  underlyingAsset
                  name
                  symbol
                  decimals
                  liquidityRate
                  variableBorrowRate
                },
                currentATokenBalance
                currentVariableDebt
                currentStableDebt                
              }
            }
        """.trimIndent()

        val response = query(query)
        val poolSharesAsString =
            JsonParser.parseString(response).asJsonObject["data"].asJsonObject["userReserves"].toString()
        return@runBlocking objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<UserReserve>>() {

            })
    }

    @Cacheable(cacheNames = ["aave-polygon-reserves"], key = "'all'")
    fun getReserves() = runBlocking {
        val query = """
            {
              reserves {
                  id
                  underlyingAsset
                  name
                  symbol
                  decimals
                  liquidityRate
                  variableBorrowRate
              }
            }
        """.trimIndent()

        val response = query(query)
        val poolSharesAsString =
            JsonParser.parseString(response).asJsonObject["data"].asJsonObject["reserves"].toString()
        return@runBlocking objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<AaveReserve>>() {

            })
    }

    fun query(query: String): String = runBlocking {
        client.request("https://api.thegraph.com/subgraphs/name/aave/aave-v2-matic") {
            method = HttpMethod.Post
            body = objectMapper.writeValueAsString(mapOf("query" to query))
        }
    }
}