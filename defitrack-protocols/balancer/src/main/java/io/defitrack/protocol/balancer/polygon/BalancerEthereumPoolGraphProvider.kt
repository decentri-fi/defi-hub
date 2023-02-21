package io.defitrack.protocol.balancer.polygon

import io.defitrack.common.network.Network
import io.defitrack.protocol.balancer.BalancerPoolGraphProvider
import io.defitrack.protocol.balancer.Pool
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class BalancerEthereumPoolGraphProvider(
    theGraphGatewayProvider: TheGraphGatewayProvider,
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/balancer-labs/balancer-v2",
    theGraphGatewayProvider
), BalancerPoolGraphProvider {

    suspend fun getPools(): List<Pool> {
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

        return query(query, "pools")
    }

    override suspend fun getPool(poolAddress: String): Pool? {
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

        return query<List<Pool>>(query, "pools").firstOrNull()
    }

    override fun getNetwork(): Network {
        return Network.ETHEREUM
    }
}