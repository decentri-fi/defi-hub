package io.defitrack.protocol.crv

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import io.defitrack.protocol.crv.dto.Gauge
import io.defitrack.protocol.crv.dto.Pool
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class CurveEthereumService(
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    fun getPools() = runBlocking {
        val query = """
            {
            	pools {
                id
                fee
                name
                swapAddress
                virtualPrice
                lpToken {
                  id
                  address
                  decimals
                  name
                	symbol
                }
                coins {
                  id
                  index
                  underlying {
                    id
                    index
                    balance,
                    token {
                      address
                      decimals
                      name
                      symbol
                    }
                  }
                }
              }
            }
        """.trimIndent()

        val response = query(query)
        val poolSharesAsString =
            JsonParser.parseString(response).asJsonObject["data"].asJsonObject["pools"].toString()
        return@runBlocking objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<Pool>>() {

            })
    }

    @Cacheable(value = ["curve-gauges"], key = "'all'")
    fun getGauges() = runBlocking {
        val query = """
            {
            	gauges {
                address
                pool {
                     id
                     name
                     swapAddress
                     virtualPrice
                     underlyingCount
                     lpToken {
                       id
                       address
                       decimals
                       name
                     	symbol
                     }
                     coins {
                       id
                       index
                       token {
                         address
                         decimals
                         name
                         symbol
                       }
                       underlying {
                         id
                         index
                         balance,
                         token {
                           address
                           decimals
                           name
                           symbol
                         }
                       }
                     }            
                }
              }
            }
        """.trimIndent()


        val response = query(query)
        val poolSharesAsString =
            JsonParser.parseString(response).asJsonObject["data"].asJsonObject["gauges"].toString()
        return@runBlocking objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<Gauge>>() {

            })
    }

    fun query(query: String): String = runBlocking {
        client.request("https://api.thegraph.com/subgraphs/name/sistemico/curve") {
            method = HttpMethod.Post
            body = objectMapper.writeValueAsString(mapOf("query" to query))
        }
    }
}