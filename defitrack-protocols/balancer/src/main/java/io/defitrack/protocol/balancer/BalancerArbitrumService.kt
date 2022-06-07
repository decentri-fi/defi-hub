package io.defitrack.protocol.balancer

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component
import java.util.*

@Component
class BalancerArbitrumService(
    private val objectMapper: ObjectMapper,
    graphGatewayProvider: TheGraphGatewayProvider
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/balancer-labs/balancer-arbitrum-v2",
    graphGatewayProvider
) {

    suspend fun getBalances(address: String): List<PoolShare> {
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

        return query(query, "poolShares")
    }
}