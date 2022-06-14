package io.defitrack.protocol.bancor

import io.defitrack.messari.LiquidityPool
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class BancorEthereumGraphProvider(
    theGraphGatewayProvider: TheGraphGatewayProvider
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/0xbe1/bancor-v3-mainnet",
    theGraphGatewayProvider
) {

    fun bancorNetwork() = "0xeef417e1d5cc832e619ae18d2f140de2999dd4fb"

    suspend fun getLiquidityPools(): List<LiquidityPool> {
        val query = """
             {
            	liquidityPools(first: 50, orderBy: totalValueLockedUSD, orderDirection: desc) {
                id
                name
                symbol,
                totalValueLockedUSD
              }
            }
        """.trimIndent()

        return query(query, "liquidityPools")
    }
}