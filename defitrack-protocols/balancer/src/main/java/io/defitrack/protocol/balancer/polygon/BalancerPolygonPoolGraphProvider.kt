package io.defitrack.protocol.balancer.polygon

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import io.defitrack.protocol.balancer.Pool
import io.defitrack.protocol.balancer.PoolShare
import io.defitrack.protocol.balancer.domain.LiquidityMiningReward
import io.defitrack.thegraph.GraphProvider
import io.defitrack.thegraph.TheGraphGatewayProvider
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.util.*
import java.util.stream.IntStream
import kotlin.streams.toList

@Component
class BalancerPolygonPoolGraphProvider(
    private val objectMapper: ObjectMapper,
    private val httpClient: HttpClient,
    theGraphGatewayProvider: TheGraphGatewayProvider,
) : GraphProvider(
    "https://api.thegraph.com/subgraphs/name/balancer-labs/balancer-polygon-v2",
    theGraphGatewayProvider
) {

    suspend fun getPools(): List<Pool> {
        val query = """
            {
               pools(first: 50, orderBy: totalLiquidity, orderDirection: desc) {
                address
                id
                totalLiquidity,
                totalShares
                tokens {
                    id
                    address
                    symbol
                    name
                    decimals
                    balance
                }
                symbol
                name
              }
            }
        """.trimIndent()

        return query(query, "pools")
    }

    suspend fun getPool(poolAddress: String): Pool? {
        val query = """
            {
              pools(where: {address: "$poolAddress"}) {
                address
                id
                totalLiquidity,
                totalShares
                tokens {
                    id
                    address
                    symbol
                    name
                    decimals
                    balance
                }
                symbol
                name
              }
            }
        """.trimIndent()

        return query<List<Pool>>(query, "pools").firstOrNull()
    }
}