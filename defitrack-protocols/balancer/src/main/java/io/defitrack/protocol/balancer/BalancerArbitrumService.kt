package io.defitrack.protocol.balancer

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.thegraph.TheGraphGatewayProvider
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.util.*

@Component
class BalancerArbitrumService(
    private val objectMapper: ObjectMapper,
    graphGatewayProvider: TheGraphGatewayProvider
) {

    val endpoint = "https://api.thegraph.com/subgraphs/name/balancer-labs/balancer-arbitrum-v2"
    val graph = graphGatewayProvider.createTheGraphGateway(endpoint)

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
        val data = graph.performQuery(query).asJsonObject["poolShares"].toString()
        return@runBlocking objectMapper.readValue(data,
            object : TypeReference<List<PoolShare>>() {

            })
    }
}