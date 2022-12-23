package io.defitrack.protocol.maplefinance

import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Service
import io.defitrack.protocol.maplefinance.domain.Market

@Service
class MapleFinanceEthereumGraphProvider(
    graphGatewayProvider: TheGraphGatewayProvider,
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/papercliplabs/messari-maple-finance",
    graphGatewayProvider
) {

    suspend fun getLendingMarkets(): List<Market> {
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