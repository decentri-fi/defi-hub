package io.defitrack.protocol

import com.google.gson.JsonParser
import io.defitrack.common.network.Network
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class HopService(private val httpClient: HttpClient) {

    val addressesUrl = "https://raw.githubusercontent.com/defitrack/data/master/protocols/hop/addresses.json";

    val cache = Cache.Builder().build<Network, List<String>>()

    fun polygonLps(network: Network): List<String> {
        return runBlocking(Dispatchers.IO) {
            cache.get(network) {
                val response: String = httpClient.get(addressesUrl)
                val assets = JsonParser.parseString(response).asJsonObject["bridges"].asJsonObject.entrySet()

                assets.flatMap {
                    it.value.asJsonObject.entrySet()
                }.filter {
                    it.value.asJsonObject.has("l2SaddleLpToken") && it.key.equals(network.slug)
                }.map {
                    it.value.asJsonObject["l2SaddleLpToken"].asString
                }
            }
        }
    }
}