package io.defitrack.protocol.maple

import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Service

@Service
class MapleFinanceEthereumGraphProvider(
    graphGatewayProvider: TheGraphGatewayProvider,
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/papercliplabs/messari-maple-finance",
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