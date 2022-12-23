package io.defitrack.protocol.balancer.polygon

import io.defitrack.protocol.balancer.domain.LiquidityGauge
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Service

@Service
class BalancerGaugeOptimismGraphProvider(
    theGraphGatewayProvider: TheGraphGatewayProvider
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/balancer-labs/balancer-gauges-optimism",
    theGraphGatewayProvider
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