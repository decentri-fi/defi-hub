package io.defitrack.price

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.math.BigDecimal
import kotlin.time.Duration.Companion.hours


class BeefyPricesService(
    private val beefyAPIEndpoint: String = "https://api.beefy.finance",
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    val cache = Cache.Builder<String, Map<String, BigDecimal>>().expireAfterWrite(1.hours).build()

    suspend fun getPrices(): Map<String, BigDecimal> {
        return withContext(Dispatchers.IO) {
            cache.get("beefy-api-prices") {
                val result: String = client.get(with(HttpRequestBuilder()) {
                    url("$beefyAPIEndpoint/prices")
                    this
                }).bodyAsText()

                objectMapper.readValue(
                    result,
                    object : TypeReference<Map<String, BigDecimal>>() {})
            }.map { entry ->
                entry.key.uppercase() to entry.value
            }.toMap()
        }
    }
}