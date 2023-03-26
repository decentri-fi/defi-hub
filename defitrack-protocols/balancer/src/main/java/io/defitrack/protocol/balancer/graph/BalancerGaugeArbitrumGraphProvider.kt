package io.defitrack.protocol.balancer.graph

import io.defitrack.protocol.balancer.domain.LiquidityGauge
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import org.springframework.stereotype.Service

@Service
class BalancerGaugeArbitrumGraphProvider(
    theGraphGatewayProvider: TheGraphGatewayProvider
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/balancer-labs/balancer-gauges-arbitrum",
    theGraphGatewayProvider
), BalancerGaugeProvider {

    override suspend fun getGauges(): List<LiquidityGauge> {
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