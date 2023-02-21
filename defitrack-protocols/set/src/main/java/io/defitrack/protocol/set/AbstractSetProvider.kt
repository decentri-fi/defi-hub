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
        return JsonParser.parseString(returnValue).asJsonObject["tokens"].asJsonArray.map {
            it.asJsonObject["address"].asString
        } + listOf(
            "0x3ad707da309f3845cd602059901e39c4dcd66473",
            "0xf287d97b6345bad3d88856b26fb7c0ab3f2c7976"
        )
    }
}