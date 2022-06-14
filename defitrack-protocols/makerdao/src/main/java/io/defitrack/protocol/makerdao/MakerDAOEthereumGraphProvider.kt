package io.defitrack.protocol.makerdao

import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Service
import io.defitrack.lending.domain.LendingMarket

@Service
class MakerDAOEthereumGraphProvider(
    graphGatewayProvider: TheGraphGatewayProvider,
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/dynamic-amm/dynamic-amm",
    graphGatewayProvider
) {

    suspend fun getLendingMarkets(): List<LendingMarket> {
        val query = """
            {
            	pools(first: 500, where: { reserveUSD_gt: 1000 }) {
                id
                pair {
                    id
                }
                reserveUSD
                token0 {
                    id,
                    symbol
                    name
                    decimals
                }
                token1 {
                  id,
                  symbol
                  name
                  decimals
                }  
              }
            }
        """.trimIndent()
        return query(query, "pools")
    }
}