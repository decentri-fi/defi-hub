package io.defitrack.protocol.ribbon

import io.defitrack.protocol.ribbon.domain.RibbonVault
import io.defitrack.protocol.thegraph.GraphProvider
import io.defitrack.protocol.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class RibbonEthereumGraphProvider(
    graphGatewayProvider: TheGraphGatewayProvider
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/ribbon-finance/ribbon-v2",
    graphGatewayProvider
) {

    suspend fun getVaults(): List<RibbonVault> {
        val query = """
            {
              vaults {
            	id
                name
                symbol
                underlyingAsset
                totalBalance
              }
            }
        """.trimIndent()
        return query(query, "vaults")
    }
}