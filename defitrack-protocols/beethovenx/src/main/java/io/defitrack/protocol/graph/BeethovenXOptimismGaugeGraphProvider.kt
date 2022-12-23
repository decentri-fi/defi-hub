package io.defitrack.protocol.graph

import io.defitrack.protocol.balancer.domain.LiquidityGauge
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Component


@Component
class BeethovenXOptimismGaugeGraphProvider(
    graphGatewayProvider: TheGraphGatewayProvider
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/beethovenxfi/balancer-gauges-optimism",
    graphGatewayProvider
) {

    suspend fun getGauges(): List<LiquidityGauge> {
        val query = """
            {
            	liquidityGauges {
                poolAddress
                id
              }
            }
        """.trimIndent()

        return query(query, "liquidityGauges")
    }
}