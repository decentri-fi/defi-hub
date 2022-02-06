package io.defitrack.protocol.balancer

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.thegraph.TheGraphGatewayProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.util.*

@Component
class BalancerPolygonService(
    private val objectMapper: ObjectMapper,
    theGraphGatewayProvider: TheGraphGatewayProvider,
) {

    private val graphGateway =
        theGraphGatewayProvider.createTheGraphGateway("https://api.thegraph.com/subgraphs/name/balancer-labs/balancer-polygon-v2-beta")

    companion object {
        val lmTokens = listOf(
            "0x9a71012b13ca4d3d0cdc72a177df3ef03b0e76a3", //balancer,
            "0x0d500b1d8e8ef31e21c99d1db9a6444d3adf1270", //WMATIC,
            "0x2e1ad108ff1d8c782fcbbb89aad783ac49586756", //TUSD,
            "0x580a84c73811e1839f75d86d75d88cca0c241ff4", //QI,
            "0xF501dd45a1198C2E1b5aEF5314A68B9006D842E0", //MTA
            "0xdf7837de1f2fa4631d716cf2502f8b230f1dcc32" //TEL
        )
    }


    fun getPools(): List<Pool> = runBlocking {
        val query = """
            {
               pools(first: 50, orderBy: totalLiquidity, orderDirection: desc) {
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

        val pools = graphGateway.performQuery(query).asJsonObject["pools"].toString()
        return@runBlocking objectMapper.readValue(pools,
            object : TypeReference<List<Pool>>() {

            })
    }

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

        val pools = graphGateway.performQuery(query).asJsonObject["pools"].toString()
        return@runBlocking objectMapper.readValue(pools,
            object : TypeReference<List<Pool>>() {

            }).firstOrNull()
    }

    fun getBalances(address: String): List<PoolShare> = runBlocking(Dispatchers.IO) {
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

        val poolShares = graphGateway.performQuery(query).asJsonObject["poolShares"].toString()
        return@runBlocking objectMapper.readValue(poolShares,
            object : TypeReference<List<PoolShare>>() {

            })
    }
}