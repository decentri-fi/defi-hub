package io.defitrack.protocol.set

import com.google.gson.JsonParser
import io.defitrack.common.network.Network
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

abstract class SetProvider(
    val network: Network,
    val tokenLocation: String,
    val httpClient: HttpClient
) {

    suspend fun getSetTokens(): List<String> {
        val returnValue = httpClient.get(tokenLocation).bodyAsText()
        return JsonParser.parseString(returnValue).asJsonObject["tokens"].asJsonArray.map {
            it.asJsonObject["address"].asString
        }
    }
}