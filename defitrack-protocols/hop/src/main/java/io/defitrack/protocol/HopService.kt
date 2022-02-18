package io.defitrack.protocol

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import io.defitrack.common.network.Network
import io.defitrack.protocol.domain.DailyVolume
import io.defitrack.protocol.domain.HopLpToken
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class HopService(
    private val client: HttpClient,
    private val objectMapper: ObjectMapper,
    private val abstractHopServices: List<AbstractHopService>
) {

    val addressesUrl = "https://raw.githubusercontent.com/defitrack/data/master/protocols/hop/addresses.json"

    val lpCache = Cache.Builder().build<Network, List<HopLpToken>>()

    fun getStakingRewards(network: Network): List<String> {
        return abstractHopServices.filter {
            it.getNetwork() == network
        }.firstOrNull()?.getStakingRewards() ?: emptyList()
    }

    fun getDailyVolumes(tokenName: String, network: Network): List<DailyVolume> {
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

        return runBlocking(Dispatchers.IO) {
            val response = query(endpoint, query)
            val poolSharesAsString =
                JsonParser.parseString(response).asJsonObject["data"].asJsonObject["dailyVolumes"].toString()
            return@runBlocking objectMapper.readValue(poolSharesAsString,
                object : TypeReference<List<DailyVolume>>() {

                })
        }
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

    fun getLps(network: Network): List<HopLpToken> {
        return runBlocking(Dispatchers.IO) {
            lpCache.get(network) {
                val response: String = client.get(addressesUrl)
                val assets = JsonParser.parseString(response).asJsonObject["bridges"].asJsonObject.entrySet()

                assets.flatMap {
                    it.value.asJsonObject.entrySet()
                }.filter {
                    it.value.asJsonObject.has("l2SaddleLpToken") && it.key.equals(network.slug)
                }.map {
                    HopLpToken(
                        lpToken = it.value.asJsonObject["l2SaddleLpToken"].asString,
                        hToken = it.value.asJsonObject["l2HopBridgeToken"].asString,
                        canonicalToken = it.value.asJsonObject["l2CanonicalToken"].asString,
                    )
                }
            }
        }
    }

    private suspend fun query(endpoint: String, query: String): String {
        return client.request(endpoint) {
            method = HttpMethod.Post
            body = objectMapper.writeValueAsString(mapOf("query" to query))
        }
    }
}