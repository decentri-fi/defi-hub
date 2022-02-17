package io.defitrack.protocol.crv

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import io.defitrack.protocol.crv.dto.Gauge
import io.defitrack.protocol.crv.dto.Pool
import io.defitrack.thegraph.TheGraphGatewayProvider
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class CurveEthereumService(
    private val objectMapper: ObjectMapper,
    theGraphGatewayProvider: TheGraphGatewayProvider
) {

    val theGraph = theGraphGatewayProvider.createTheGraphGateway("https://api.thegraph.com/subgraphs/name/curvefi/curve")

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

        val poolSharesAsString =
            theGraph.performQuery(query).asJsonObject["pools"].toString()
        return@runBlocking objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<Pool>>() {

            })
    }

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


        val poolSharesAsString =
            theGraph.performQuery(query).asJsonObject["gauges"].toString()
        return@runBlocking objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<Gauge>>() {

            })
    }
}