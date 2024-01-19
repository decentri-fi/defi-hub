package io.defitrack.protocol.thegraph

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TheGraphGateway(
    private val httpClient: HttpClient,
    val objectMapper: ObjectMapper,
    private val baseUrl: String
) {

    suspend fun performQuery(query: String): JsonElement {
        val response = query(query)
        return JsonParser.parseString(response).asJsonObject["data"]
    }

    private suspend fun query(query: String): String = withContext(Dispatchers.IO){
        val response: String = httpClient.post(baseUrl) {

            headers {
                append("Content-Type", "application/json")
            }
            setBody(GraphRequest(query))
        }.bodyAsText()
        response
    }
}