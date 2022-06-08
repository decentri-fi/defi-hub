package io.defitrack.protocol

import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import io.defitrack.protocol.dodo.domain.Pair as DodoPair

abstract class DodoGraphProvider(url: String, graphGatewayProvider: TheGraphGatewayProvider) :
    GraphProvider(url, graphGatewayProvider) {

    suspend fun getPools(): List<DodoPair> {
        val query = """
            {
            pairs(orderBy: volumeUSD, orderDirection: desc) {
               id
              baseToken {
                id
              }
              quoteToken {
                id
              }
                volumeUSD
                isTradeAllowed
            }
        }
        """.trimIndent()
        return query(query, "liquidityPools")
    }
}