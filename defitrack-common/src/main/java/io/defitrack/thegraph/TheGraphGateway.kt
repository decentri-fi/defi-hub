package io.defitrack.thegraph

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

class TheGraphGateway(
    private val httpClient: HttpClient,
    private val objectMapper: ObjectMapper,
    private val baseUrl: String
) {

    suspend fun performQuery(query: String): JsonElement {
        val response = query(query)
        return JsonParser.parseString(response).asJsonObject["data"]
    }

    private suspend fun query(query: String): String {
        return httpClient.request(baseUrl) {
            method = HttpMethod.Post
            body = objectMapper.writeValueAsString(mapOf("query" to query))
        }
    }
}