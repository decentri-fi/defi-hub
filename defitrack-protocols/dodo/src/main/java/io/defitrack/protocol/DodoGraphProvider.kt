package io.defitrack.protocol

import io.defitrack.protocol.domain.LiquidityPool
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider

abstract class DodoGraphProvider(url: String, graphGatewayProvider: TheGraphGatewayProvider) :
    GraphProvider(url, graphGatewayProvider) {

    suspend fun getPools(): List<LiquidityPool> {
        val query = """
            { 
              liquidityPools(first: 50, orderBy: totalValueLockedUSD, orderDirection: desc) {
            		id
                name
                totalValueLockedUSD
                inputTokenWeights 
                inputTokens {
                  id
                  symbol
                }
              }
            }
        """.trimIndent()
        return query(query, "liquidityPools")
    }
}