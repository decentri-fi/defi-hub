package io.defitrack.thegraph

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonElement
import io.ktor.client.*
import org.springframework.stereotype.Component

@Component
class TheGraphGatewayProvider(
    private val httpClient: HttpClient,
    val objectMapper: ObjectMapper
) {
    fun createTheGraphGateway(baseUrl: String) = TheGraphGateway(
        httpClient,
        objectMapper,
        baseUrl
    )
}