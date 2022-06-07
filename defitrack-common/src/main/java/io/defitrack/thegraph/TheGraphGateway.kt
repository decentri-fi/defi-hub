package io.defitrack.thegraph

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class TheGraphGateway(
    private val httpClient: HttpClient,
    val objectMapper: ObjectMapper,
    private val baseUrl: String
) {

    inline fun <reified T>map(data: JsonElement): T {
        return objectMapper.readValue(data.toString(),
            object : TypeReference<T>() {

            })
    }

    suspend fun performQuery(query: String): JsonElement {
        val response = query(query)
        return JsonParser.parseString(response).asJsonObject["data"]
    }

    private suspend fun query(query: String): String {
        val response: String = httpClient.request(baseUrl) {
            method = HttpMethod.Post
            setBody(objectMapper.writeValueAsString(mapOf("query" to query)))
        }.body()
        return response
    }
}