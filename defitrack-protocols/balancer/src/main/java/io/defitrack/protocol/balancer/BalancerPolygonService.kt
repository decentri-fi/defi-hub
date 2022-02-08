package io.defitrack.protocol.balancer

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import io.defitrack.protocol.balancer.domain.LiquidityMiningReward
import io.defitrack.thegraph.TheGraphGatewayProvider
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.util.*
import java.util.stream.IntStream
import kotlin.streams.toList

@Component
class BalancerPolygonService(
    private val objectMapper: ObjectMapper,
    private val httpClient: HttpClient,
    theGraphGatewayProvider: TheGraphGatewayProvider,
) {

    private val graphGateway =
        theGraphGatewayProvider.createTheGraphGateway("https://api.thegraph.com/subgraphs/name/balancer-labs/balancer-polygon-v2-beta")

    companion object {
        val lmTokens = listOf(
            "0x9a71012b13ca4d3d0cdc72a177df3ef03b0e76a3", //balancer,
            "0x0d500b1d8e8ef31e21c99d1db9a6444d3adf1270", //WMATIC,
            "0x2e1ad108ff1d8c782fcbbb89aad783ac49586756", //TUSD,
            "0x580a84c73811e1839f75d86d75d88cca0c241ff4", //QI,
            "0xF501dd45a1198C2E1b5aEF5314A68B9006D842E0", //MTA
            "0xdf7837de1f2fa4631d716cf2502f8b230f1dcc32" //TEL
        )
    }

    suspend fun findLatestLmWeek(): Int {
        var int = 86 //dev's knowledge
        return try {
            while (true) {
                int++
                httpClient.get<String>("https://raw.githubusercontent.com/balancer-labs/bal-mining-scripts/master/reports/$int/_totals.json")
            }
            int - 1 //unused but necessary
        } catch (ex: Exception) {
            int - 1
        }
    }

    fun getRewards(): List<LiquidityMiningReward> = runBlocking {
        val lastWeek = findLatestLmWeek()
        IntStream.rangeClosed(1, lastWeek).toList().flatMap { week ->
            getRewardsForWeek(week)
        }
    }

    private fun getRewardsForWeek(week: Int): List<LiquidityMiningReward> {
        return lmTokens.flatMap { token ->
            getRewardsForWeekAndToken(week, token)
        }
    }

    private fun getRewardsForWeekAndToken(week: Int, token: String): List<LiquidityMiningReward> = runBlocking {
        try {
            val response: String = httpClient.get(
                "https://raw.githubusercontent.com/balancer-labs/bal-mining-scripts/master/reports/$week/__polygon_$token.json"
            )

            JsonParser.parseString(response).asJsonObject.entrySet().map { entry ->
                LiquidityMiningReward(
                    entry.key,
                    token,
                    entry.value.asBigDecimal,
                    week
                )
            }
        } catch (ex: Exception) {
            emptyList()
        }
    }

    fun getPools(): List<Pool> = runBlocking {
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

        val pools = graphGateway.performQuery(query).asJsonObject["pools"].toString()
        return@runBlocking objectMapper.readValue(pools,
            object : TypeReference<List<Pool>>() {

            })
    }

    fun getPool(poolAddress: String): Pool? = runBlocking {
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

        val pools = graphGateway.performQuery(query).asJsonObject["pools"].toString()
        return@runBlocking objectMapper.readValue(pools,
            object : TypeReference<List<Pool>>() {

            }).firstOrNull()
    }

    fun getBalances(address: String): List<PoolShare> = runBlocking(Dispatchers.IO) {
        val query = """
            {
              poolShares(where: {userAddress: "${address.lowercase(Locale.getDefault())}"}) {
                userAddress {
                  id
                },
                poolId {
                  id
                  totalShares
                  totalLiquidity
                  address
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
                },
                balance
              }
           }
        """.trimIndent()

        val poolShares = graphGateway.performQuery(query).asJsonObject["poolShares"].toString()
        return@runBlocking objectMapper.readValue(poolShares,
            object : TypeReference<List<PoolShare>>() {

            })
    }
}