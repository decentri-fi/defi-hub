package io.defitrack.protocol.set

import com.google.gson.JsonParser
import io.defitrack.common.network.Network
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

abstract class AbstractSetProvider(
    val network: Network,
    val tokenLocation: String,
    val httpClient: HttpClient
) : SetProvider {

    override suspend fun getSets(): List<String> {
        val returnValue = httpClient.get(tokenLocation).bodyAsText()
        return JsonParser.parseString(returnValue).asJsonObject["tokens"].asJsonArray.filter {
            it.asJsonObject["chainId"] == null ||  it.asJsonObject["chainId"].asInt == network.chainId
        }.map {
            it.asJsonObject["address"].asString
        } + extraSets()
    }

    open fun extraSets(): List<String> = emptyList()
}