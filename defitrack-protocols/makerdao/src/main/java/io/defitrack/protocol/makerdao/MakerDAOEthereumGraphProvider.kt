package io.defitrack.protocol.makerdao

import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Service
import io.defitrack.lending.domain.LendingMarket

@Service
class MakerDAOEthereumGraphProvider(
    graphGatewayProvider: TheGraphGatewayProvider,
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/messari/makerdao-ethereum",
    graphGatewayProvider
) {

    suspend fun getLendingMarkets(): List<LendingMarket> {
        val query = """
            {
                markets( where: { isActive: true }) {
                    id
                    name
                    rates {id, rate, type}
                  }
            }
        """.trimIndent()
        return query(query, "markets")
    }
}