package io.defitrack.protocol.makerdao

import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Service
import io.defitrack.protocol.makerdao.domain.Market

@Service
class MakerDAOEthereumGraphProvider(
    graphGatewayProvider: TheGraphGatewayProvider,
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/messari/makerdao-ethereum",
    graphGatewayProvider
) {

    suspend fun getMarkets(): List<Market> {
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