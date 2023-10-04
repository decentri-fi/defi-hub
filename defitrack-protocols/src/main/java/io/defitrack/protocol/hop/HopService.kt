package io.defitrack.protocol.hop

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import io.defitrack.common.network.Network
import io.defitrack.protocol.hop.domain.DailyVolume
import io.defitrack.protocol.hop.domain.HopLpToken
import io.defitrack.thegraph.TheGraphGatewayProvider
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

@Component
class HopService(
    private val client: HttpClient,
    private val objectMapper: ObjectMapper,
    private val abstractHopServices: List<AbstractHopService>,
    private val graphGatewayProvider: TheGraphGatewayProvider
) {

    val addressesUrl = "https://raw.githubusercontent.com/defitrack/data/master/protocols/hop/addresses.json"

    val lpCache = Cache.Builder<Network, List<HopLpToken>>().build()

    fun getStakingRewards(network: Network): List<String> {
        return abstractHopServices.firstOrNull {
            it.getNetwork() == network
        }?.getStakingRewards() ?: emptyList()
    }

    suspend fun getDailyVolumes(tokenName: String, network: Network): List<DailyVolume> {
        val token = convertTokenName(tokenName)
        val query = """
            {
            	dailyVolumes(first: 8, orderBy: date, orderDirection: desc where: {token: "$token"}) {
                id
                amount
                date
              }
            }
        """.trimIndent()
        val endpoint = getGraph(network)
        val graph = graphGatewayProvider.createTheGraphGateway(endpoint)

        val poolSharesAsString = graph.performQuery(query).asJsonObject["dailyVolumes"].toString()
        return objectMapper.readValue(poolSharesAsString,
            object : TypeReference<List<DailyVolume>>() {

            })
    }

    private fun convertTokenName(tokenName: String) = when (tokenName) {
        "WMATIC" -> "MATIC"
        "WETH" -> "ETH"
        "WBTC" -> "BTC"
        else -> tokenName
    }

    private fun getGraph(network: Network): String {
        return abstractHopServices.firstOrNull {
            it.getNetwork() == network
        }?.getGraph() ?: throw IllegalArgumentException("$network not supported")
    }

    suspend fun getLps(network: Network): List<HopLpToken> {
        return lpCache.get(network) {
            val response: String = withContext(Dispatchers.IO) { client.get(addressesUrl).bodyAsText() }
            val assets = JsonParser.parseString(response).asJsonObject["bridges"].asJsonObject.entrySet()

            assets.flatMap {
                it.value.asJsonObject.entrySet()
            }.filter {
                it.value.asJsonObject.has("l2SaddleLpToken") && it.key.equals(network.slug)
            }.map {
                HopLpToken(
                    lpToken = it.value.asJsonObject["l2SaddleLpToken"].asString,
                    hToken = it.value.asJsonObject["l2HopBridgeToken"].asString,
                    swapAddress = it.value.asJsonObject["l2SaddleSwap"].asString,
                    canonicalToken = it.value.asJsonObject["l2CanonicalToken"].asString,
                )
            }
        }.filter {
            it.lpToken != "0x0000000000000000000000000000000000000000"
        }
    }
}