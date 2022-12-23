package io.defitrack.protocol.crv

import io.defitrack.protocol.crv.domain.LPToken
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class CurvePolygonGraphProvider(
    graphGatewayProvider: TheGraphGatewayProvider
) : GraphProvider("https://api.thegraph.com/subgraphs/name/gvladika/curve-polygon", graphGatewayProvider) {

    suspend fun getPool(id: String) : LPToken? {
        val query = """
          
        """.trimIndent()
        return query(query, "lptoken")
    }

    suspend fun getPools(): List<LPToken> {
        val query = """
            {
            	lptokens(first: 500) {
                id
                token {
                  id
                }
                pool {
                  balances
                  coins {
                    id
                  }
                }
              }
            }
        """.trimIndent()

        return query(query, "lptokens")
    }
}