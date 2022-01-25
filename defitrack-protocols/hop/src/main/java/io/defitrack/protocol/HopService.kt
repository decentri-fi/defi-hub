package io.defitrack.protocol

import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class HopService(private val httpClient: HttpClient) {

    val addressesUrl = "https://raw.githubusercontent.com/defitrack/data/master/protocols/hop/addresses.json";

    fun polygonLps() : List<String> {
        return runBlocking {
            val response: String = httpClient.get(addressesUrl)
            val assets = JsonParser.parseString(response).asJsonObject["bridges"].asJsonArray

             assets.map {
                it.asJsonObject["polygon"].asJsonObject
            }.map {
                it["l2SaddleLpToken"].asString
             }
        }
    }
}