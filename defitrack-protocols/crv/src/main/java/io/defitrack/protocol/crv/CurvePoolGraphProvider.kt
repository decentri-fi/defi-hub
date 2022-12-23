package io.defitrack.protocol.crv

import io.defitrack.common.network.Network
import io.defitrack.protocol.crv.domain.Pool
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider

abstract class CurvePoolGraphProvider(
    url: String,
    graphGatewayProvider: TheGraphGatewayProvider,
    val network: Network
) : GraphProvider(url, graphGatewayProvider) {

    suspend fun getPoolByLp(address: String): Pool? {
        val query = """
          {
                pools(where: {lpToken: "$address"}) {
                address
                lpToken
                coins
          }
        }
        """.trimIndent()

        val pools: List<Pool> = query(query, "pools")
        return pools.firstOrNull()
    }


    suspend fun getPools(): List<Pool> {
        val query = """
        {
            pools(first: 250, where: {cumulativeVolumeUSD_gt: 10000}) {
            address
            lpToken
            coins
          }
        }
        """.trimIndent()

        return query(query, "pools")
    }
}