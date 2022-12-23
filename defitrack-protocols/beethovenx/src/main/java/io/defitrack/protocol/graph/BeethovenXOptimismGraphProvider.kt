package io.defitrack.protocol.graph

import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component
import io.defitrack.protocol.balancer.Pool

@Component
class BeethovenXOptimismGraphProvider(
     graphGatewayProvider: TheGraphGatewayProvider
): GraphProvider(
    "https://api.thegraph.com/subgraphs/name/beethovenxfi/beethovenx-optimism",
    graphGatewayProvider
) {

    suspend fun getPools(): List<Pool> {
        val query = """
            {
               pools(first: 100, orderBy: totalLiquidity, orderDirection: desc) {
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

        return query(query, "pools")
    }

}