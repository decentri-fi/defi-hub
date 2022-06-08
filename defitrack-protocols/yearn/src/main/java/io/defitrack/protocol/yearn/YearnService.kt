package io.defitrack.protocol.yearn

import com.fasterxml.jackson.databind.ObjectMapper
import io.defitrack.protocol.yearn.domain.YearnV2Vault
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component

@Component
class YearnService(
    private val objectMapper: ObjectMapper,
    graphGatewayProvider: TheGraphGatewayProvider,
) : GraphProvider("https://api.thegraph.com/subgraphs/name/rareweasel/yearn-vaults-v2-mainnet", graphGatewayProvider) {

    suspend fun provideYearnV2Vaults(): List<YearnV2Vault> {
        val query = """
                 {
                vaults {
                  id
                  token {
                    id
                    name
                    symbol
                    decimals
                  }
                  shareToken {
                    id
                    name
                    symbol
                    decimals
                  }
                  registry {
                    id
                  }
                  apiVersion
                } 
                }
            """.trimIndent()
        return query(query, "vaults")
    }
}