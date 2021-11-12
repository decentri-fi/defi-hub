package io.codechef.price

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.math.BigDecimal
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Service
class BeefyPricesService(
    private val beefyAPIEndpoint: String = "https://api.beefy.finance",
    private val objectMapper: ObjectMapper,
    private val client: HttpClient
) {

    @OptIn(ExperimentalTime::class)
    val cache = Cache.Builder().expireAfterWrite(Duration.Companion.hours(1)).build<String, Map<String, BigDecimal>>()

    fun getPrices(): Map<String, BigDecimal> {
        return runBlocking {
            cache.get("beefy-api-prices") {
                val result = client.get<String>(with(HttpRequestBuilder()) {
                    url("$beefyAPIEndpoint/prices")
                    this
                })

                objectMapper.readValue(
                    result,
                    object : TypeReference<Map<String, BigDecimal>>() {})
            }.map { entry ->
                entry.key.uppercase() to entry.value
            }.toMap()
        }
    }
}