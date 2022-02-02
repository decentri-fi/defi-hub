package io.defitrack.thegraph

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import org.springframework.stereotype.Component

@Component
class TheGraphGatewayProvider(
    private val httpClient: HttpClient,
    private val objectMapper: ObjectMapper
) {

    fun createTheGraphGateway(baseUrl: String) = TheGraphGateway(
        httpClient,
        objectMapper,
        baseUrl
    )
}